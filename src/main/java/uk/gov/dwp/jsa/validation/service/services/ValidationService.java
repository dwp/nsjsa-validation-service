package uk.gov.dwp.jsa.validation.service.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.dwp.jsa.validation.service.adaptors.BookingStatusAdaptor;
import uk.gov.dwp.jsa.validation.service.adaptors.ClaimStatusAdaptor;
import uk.gov.dwp.jsa.validation.service.exceptions.ClaimantAlreadyExistsException;
import uk.gov.dwp.jsa.validation.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.validation.service.gateway.ClaimantServiceClient;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;
import uk.gov.dwp.jsa.validation.service.models.db.ClaimStatus;
import uk.gov.dwp.jsa.validation.service.models.db.PushStatus;
import uk.gov.dwp.jsa.validation.service.models.db.Status;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;
import uk.gov.dwp.jsa.validation.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusType;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatusRepository;
import uk.gov.dwp.jsa.validation.service.util.PushStatusFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

    private ClaimStatusRepository claimStatusRepository;

    private ClaimStatusAdaptor claimStatusAdaptor;

    private ClaimantServiceClient claimantServiceClient;

    @Autowired
    public ValidationService(
            final ClaimStatusRepository claimStatusRepository,
            final ClaimStatusAdaptor claimStatusAdaptor,
            final ClaimantServiceClient claimantServiceClient
    ) {
        this.claimStatusRepository = claimStatusRepository;
        this.claimStatusAdaptor = claimStatusAdaptor;
        this.claimantServiceClient = claimantServiceClient;
    }

    public ClaimStatusResponse getClaimStatusByClaimantId(final UUID id) {
        ClaimStatus claimStatus = claimStatusRepository
                .findByClaimantId(id)
                .orElseThrow(ClaimantByIdNotFoundException::new);

        String hash = DigestUtils.sha256Hex(
                claimStatus.getClaimantId().toString()
        );

        if (!hash.equals(claimStatus.getHash())) {
            throw new HttpServerErrorException(HttpStatus.CONFLICT, "HashCodes Don't Match, Data Tampered");
        }

        return claimStatusAdaptor.toResponse(claimStatus);
    }

    public UUID createClaimStatus(final UUID claimantId, final ClaimStatusRequest claimStatusRequest) {
        claimStatusRequest.setClaimantId(claimantId);
        ClaimStatus claimStatus = claimStatusAdaptor.fromRequest(claimStatusRequest);
        String hash = DigestUtils.sha256Hex(
                claimStatus.getClaimantId().toString()
        );
        claimStatus.setHash(hash);

        final PushStatus pushStatus = PushStatusFactory.defaultPushStatus();
        claimStatus.getPushStatuses().add(pushStatus);
        pushStatus.setClaimStatus(claimStatus);

        ClaimStatus savedClaimStatus;
        try {
            savedClaimStatus = claimStatusRepository.save(claimStatus);
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Error, failed to save entity due to: ", e);
            throw new ClaimantAlreadyExistsException();
        }

        claimantServiceClient.sendLatestStatus(claimantId, createClaimantRequestDtoWith(savedClaimStatus));

        return savedClaimStatus.getClaimStatusId();
    }

    public UUID addBookingStatus(final UUID claimantId, final ClaimStatusRequest claimStatusRequest) {
        ClaimStatus claimStatus = claimStatusRepository
                .findByClaimantId(claimantId)
                .orElseThrow(ClaimantByIdNotFoundException::new);

        BookingStatusRequest bookingStatusRequest = claimStatusRequest.getBookingStatus();
        BookingStatus bookingStatus = BookingStatusAdaptor.fromRequest(bookingStatusRequest);

        claimStatus.getBookingStatuses().add(bookingStatus);
        bookingStatus.setClaimStatus(claimStatus);

        ClaimStatus savedClaimStatus = claimStatusRepository.save(claimStatus);

        claimantServiceClient.sendLatestStatus(claimantId, createClaimantRequestDtoWith(savedClaimStatus));

        return savedClaimStatus.getClaimStatusId();
    }

    public void updatePushStatus(final UUID claimantId, final PushStatusType pushStatusType) {
        Objects.requireNonNull(claimantId);
        Objects.requireNonNull(pushStatusType);

        ClaimStatus claimStatus = claimStatusRepository.findByClaimantId(claimantId)
                .orElseThrow(ClaimantByIdNotFoundException::new);

        PushStatus pushStatus = new PushStatus(
                pushStatusType.name(),
                null,
                LocalDateTime.now(),
                DigestUtils.sha256Hex(pushStatusType.name())
        );

        claimStatus.getPushStatuses().add(pushStatus);
        pushStatus.setClaimStatus(claimStatus);

        final ClaimStatus savedClaimStatus = claimStatusRepository.save(claimStatus);

        claimantServiceClient.sendLatestStatus(claimantId, createClaimantRequestDtoWith(savedClaimStatus));

    }

    private CurrentStatusDto createClaimantRequestDtoWith(final ClaimStatus claimStatus) {
        final CurrentStatusDto currentStatusDto = new CurrentStatusDto();
        final List<Status> pushStatuses = new ArrayList<>(claimStatus.getPushStatuses());
        final List<Status> bookingStatuses = new ArrayList<>(claimStatus.getBookingStatuses());
        final Comparator<Status> byCreatedTimestamp = Comparator.comparing(Status::getCreatedTimestamp);

        if (!pushStatuses.isEmpty()) {
            pushStatuses.stream().max(byCreatedTimestamp).ifPresent(status -> {
                currentStatusDto.setPushStatus(
                        new CurrentStatusDto.StatusDto(status.getStatus(), status.getSubstatus())
                );
                currentStatusDto.setCreatedTimestamp(status.getCreatedTimestamp());
            });
        }

        if (!bookingStatuses.isEmpty()) {
            bookingStatuses.stream().max(byCreatedTimestamp).ifPresent(status -> {
                currentStatusDto.setBookingStatus(
                        new CurrentStatusDto.StatusDto(status.getStatus(), status.getSubstatus())
                );
                currentStatusDto.setCreatedTimestamp(status.getCreatedTimestamp());
            });
        }

        return currentStatusDto;

    }

    public void invalidateClaimStatuses(final List<UUID> claimantIds) {
        claimStatusRepository.invalidateStatuses(claimantIds);
    }

}
