package uk.gov.dwp.jsa.validation.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.validation.service.services.ClaimStatisticsService;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;

import static uk.gov.dwp.jsa.validation.service.config.WithVersionUriComponentsBuilder.VERSION_SPEL;


@RestController
@RequestMapping("/nsjsa/" + VERSION_SPEL + "/claim/statistics")
public class ClaimStatisticsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimStatisticsController.class);

    private ResponseFactory responseFactory;
    private ClaimStatisticsService claimStatisticsService;

    @Autowired
    public ClaimStatisticsController(
            final ResponseFactory responseFactory,
            final ClaimStatisticsService claimStatisticsService) {
        this.responseFactory = responseFactory;
        this.claimStatisticsService = claimStatisticsService;
    }

    @PreAuthorize("hasAnyAuthority('CCM')")
    @GetMapping
    public ResponseEntity<ApiResponse<ClaimStatistics>> getClaimStatistics(
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            final HttpServletRequest request
    ) {
        LOGGER.debug("Getting claim statistics for {}", date);
        final ClaimStatistics claimStatistics = claimStatisticsService.getAllClaimStatistics(date);
        return responseFactory.create(
                request.getRequestURI(),
                claimStatistics
        );
    }

    @PreAuthorize("hasAnyAuthority('CCM')")
    @GetMapping("agent")
    public ResponseEntity<ApiResponse<AgentPerformances>> getAgentPerformance(
            @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
            @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate,
            final HttpServletRequest request
            ) {
        LOGGER.debug("Getting agent performance for {} to {}", startDate, endDate);
        final AgentPerformances agentPerformances = claimStatisticsService.getAgentPerformances(startDate, endDate);
        return responseFactory.create(
                request.getRequestURI(),
                agentPerformances
        );
    }


}
