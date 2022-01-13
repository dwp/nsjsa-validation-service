package uk.gov.dwp.jsa.validation.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.adaptors.dto.claim.validation.PushClaimResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.validation.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;
import uk.gov.dwp.jsa.validation.service.services.PushClaimService;
import uk.gov.dwp.jsa.validation.service.services.ValidationService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromController;
import static uk.gov.dwp.jsa.validation.service.config.WithVersionUriComponentsBuilder.VERSION_SPEL;


@RestController
@RequestMapping("/nsjsa/" + VERSION_SPEL + "/claim")
public class ValidationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationController.class);

    private final ValidationService validationService;
    private final PushClaimService pushClaimService;
    private final WithVersionUriComponentsBuilder uriBuilder;
    private ResponseFactory responseFactory;

    @Autowired
    public ValidationController(
            final ValidationService validationService,
            final PushClaimService pushClaimService,
            final WithVersionUriComponentsBuilder uriBuilder,
            final ResponseFactory responseFactory
    ) {
        this.validationService = validationService;
        this.pushClaimService = pushClaimService;
        this.uriBuilder = uriBuilder;
        this.responseFactory = responseFactory;
    }

    @PreAuthorize("hasAnyAuthority('WC', 'CCM', 'CCA')")
    @GetMapping("/{id}/validation")
    public ResponseEntity<ApiResponse<ClaimStatusResponse>> getClaimStatus(
            @PathVariable final UUID id,
            final HttpServletRequest request
    ) {
        LOGGER.debug("Getting claim status for id: {}", id);
        return responseFactory.create(
                request.getRequestURI(),
                validationService.getClaimStatusByClaimantId(id)
        );
    }

    @PreAuthorize("!hasAnyAuthority('SCA')")
    @PostMapping("/{id}/validation")
    public ResponseEntity<ApiResponse<UUID>> createClaimStatus(
            @PathVariable("id") final UUID claimantId,
            @RequestBody @Validated final ClaimStatusRequest claimStatusRequest
    ) {
        LOGGER.debug("Creating claim status for claimantId: {}", claimantId);
        final UUID claimStatusId = validationService.createClaimStatus(claimantId, claimStatusRequest);

        return responseFactory.createSuccessfulResponse(
                buildResourceUriFor(claimantId),
                claimStatusId,
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyAuthority('WC', 'CCM', 'CCA')")
    @PostMapping("/{id}/push")
    public ResponseEntity<ApiResponse<PushClaimResponse>> pushClaim(
            @PathVariable("id") final UUID claimantId) throws ExecutionException, InterruptedException {
        LOGGER.debug("Pushing claim for claimantId: {}", claimantId);

        final PushClaimResponse pushResponse = pushClaimService.pushClaim(claimantId);

        return responseFactory.createSuccessfulResponse(
                buildResourceUriFor(claimantId),
                pushResponse,
                HttpStatus.ACCEPTED
        );
    }

    @PreAuthorize("hasAnyAuthority('WC', 'CCA', 'CCM')")
    @PostMapping("/{id}/booking-status")
    public ResponseEntity<ApiResponse<UUID>> addBookingStatus(
            @PathVariable("id") final UUID claimantId,
            @RequestBody @Validated final ClaimStatusRequest claimStatusRequest
    ) {
        LOGGER.debug("Adding booking status for claimantId: {}", claimantId);
        final UUID claimStatusId = validationService.addBookingStatus(claimantId, claimStatusRequest);

        if (claimStatusRequest.getBookingStatus() != null
                && claimStatusRequest.getBookingStatus().getStatus() == BookingStatusType.SUCCESS) {
            if (pushClaimService.pushClaimDrs(claimantId)) {
                LOGGER.info("Pushed claim {} to DRS successfully", claimantId);
            } else {
                LOGGER.error("Failed to pushed claim {} to DRS", claimantId);
            }
        }

        return responseFactory.createSuccessfulResponse(
                buildResourceUriFor(claimantId),
                claimStatusId,
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("!hasAnyAuthority('WC', 'SCA')")
    @PutMapping("/invalidate")
    public ResponseEntity<ApiResponse<Boolean>> invalidateClaimStatus(@RequestBody final List<UUID> claimantIds) {
        validationService.invalidateClaimStatuses(claimantIds);
        final boolean updated = true;
        return responseFactory.createSuccessfulResponse(
                fromController(uriBuilder, getClass())
                        .path("/invalidate")
                        .build()
                        .getPath(),
                updated,
                HttpStatus.ACCEPTED
        );
    }

    private String buildResourceUriFor(final UUID claimantId) {
        return fromController(uriBuilder, getClass())
                .path("/claim")
                .path("/{id}")
                .path("/validation")
                .buildAndExpand(claimantId)
                .toUri().getPath();
    }

}
