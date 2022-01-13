package uk.gov.dwp.jsa.validation.service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatisticsRepository;

import java.time.LocalDate;

@Component
public class ClaimStatisticsService {

    private ClaimStatisticsRepository claimStatisticsRepository;

    @Autowired
    public ClaimStatisticsService(final ClaimStatisticsRepository claimStatisticsRepository) {
        this.claimStatisticsRepository = claimStatisticsRepository;
    }

    public ClaimStatistics getClaimStatistics(final LocalDate date) {
        ClaimStatistics results = claimStatisticsRepository.getAllClaimStatistics(date);

        return results;
    }

    public AgentPerformances getAgentPerformances(final LocalDate from, final LocalDate to) {
        AgentPerformances results = claimStatisticsRepository.getAgentPerformances(from, to);

        return results;
    }
}
