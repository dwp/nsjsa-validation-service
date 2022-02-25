package uk.gov.dwp.jsa.validation.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatisticsRepository;

import java.time.LocalDate;

@Component
public class ClaimStatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimStatisticsService.class);

    private ClaimStatisticsRepository claimStatisticsRepository;

    @Autowired
    public ClaimStatisticsService(final ClaimStatisticsRepository claimStatisticsRepository) {
        this.claimStatisticsRepository = claimStatisticsRepository;
    }

    public ClaimStatistics getAllClaimStatistics(final LocalDate date) {
        LOGGER.debug("Getting claim statistics from database");
        return claimStatisticsRepository.getAllClaimStatistics(date);
    }

    public AgentPerformances getAgentPerformances(final LocalDate from, final LocalDate to) {
        AgentPerformances results = claimStatisticsRepository.getAgentPerformances(from, to);

        return results;
    }
}
