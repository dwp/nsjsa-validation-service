package uk.gov.dwp.jsa.validation.service.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformance;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClaimStatisticsServiceTest {
    private static final LocalDateTime OLDEST_CLAIM_OPEN = LocalDateTime.of(2019,1,1, 11, 12, 13);
    private static final int CASES_RECEIVED_IN_DAY = 1;
    private static final int HEAD_OF_WORK = 2;
    private static final int CASES_CLEARED_IN_DAY = 3;
    private static final double PERCENTAGE_OF_CLAIMS_IN_DAY_CLOSED_IN_24_HR = 4;
    private static final double PERCENTAGE_OF_CLAIMS_IN_DAY_CLOSED_IN_48_HR = 5;
    private static final int TOTAL_NUMBER_OF_CLAIMS_IN_WEEK = 6;
    private static final double PERCENTAGE_OF_CLAIMS_IN_WEEK_CLOSED_IN_24_HR = 7;
    private static final double PERCENTAGE_OF_CLAIMS_IN_WEEK_CLOSED_IN_48_HR = 8;
    private static final int OUTSTANDING_OUTSIDE_24HR = 9;
    private static final int OUTSTANDING_OUTSIDE_48HR = 10;
    private static final LocalDate startDate = LocalDate.now();
    private static final LocalDate endDate = startDate.plusDays(3);

    private ClaimStatisticsService service;
    private ClaimStatistics claimStatistics;
    private AgentPerformances agentPerformances;

    @Mock
    private ClaimStatisticsRepository claimStatisticsRepository;

    @Before
    public void beforeEachTest() {

        initMocks(this);

        ClaimStatistics expected = new ClaimStatistics(
                1, 2, OLDEST_CLAIM_OPEN, 3, 4d, 5d, 6, 7d, 8d, 9, 10
        );

        when(claimStatisticsRepository.getAllClaimStatistics(startDate)).thenReturn(expected);

        AgentPerformances expectedAgent = new AgentPerformances(startDate, endDate,
                new AgentPerformance[] {
                        new AgentPerformance("Agent 1", 1,1,1,1),
                        new AgentPerformance("Agent 2", 1,1,1,1),
                });

        when(claimStatisticsRepository.getAgentPerformances(startDate, startDate)).thenReturn(expectedAgent);
    }

    @Test
    public void getsClaimStatistics() {
        givenAService();
        whenIGetClaimStatistics();
        thenTheClaimStatisticsAreReturned();

    }

    @Test
    public void getAgentPerformances() {
        givenAService();
        whenIGetAgentPerformances();
        thenTheAgentPerformancesAreReturned();
    }

    private void givenAService() {
        service = new ClaimStatisticsService(claimStatisticsRepository);
    }

    private void whenIGetClaimStatistics() {
        claimStatistics = service.getClaimStatistics(startDate);
    }

    private void whenIGetAgentPerformances() {
        agentPerformances = service.getAgentPerformances(startDate, startDate);
    }


    private void thenTheClaimStatisticsAreReturned() {
        assertThat(claimStatistics.getOldestClaimOpen(), is(notNullValue()));
        assertThat(claimStatistics.getCasesReceivedInDay(), is(CASES_RECEIVED_IN_DAY));
        assertThat(claimStatistics.getCasesClearedInDay(), is(CASES_CLEARED_IN_DAY));
        assertThat(claimStatistics.getPercentageOfClaimsInDayClosedIn24hr(), is(PERCENTAGE_OF_CLAIMS_IN_DAY_CLOSED_IN_24_HR));
        assertThat(claimStatistics.getPercentageOfClaimsInDayClosedIn48hr(), is(PERCENTAGE_OF_CLAIMS_IN_DAY_CLOSED_IN_48_HR));
        assertThat(claimStatistics.getTotalNumberOfClaimsInWeek(), is(TOTAL_NUMBER_OF_CLAIMS_IN_WEEK));
        assertThat(claimStatistics.getPercentageOfClaimsInWeekClosedIn24hr(), is(PERCENTAGE_OF_CLAIMS_IN_WEEK_CLOSED_IN_24_HR));
        assertThat(claimStatistics.getPercentageOfClaimsInWeekClosedIn48hr(), is(PERCENTAGE_OF_CLAIMS_IN_WEEK_CLOSED_IN_48_HR));
        assertThat(claimStatistics.getHeadOfWork(), is(HEAD_OF_WORK));
    }

    private void thenTheAgentPerformancesAreReturned() {
        assertThat(agentPerformances, is(notNullValue()));
        assertThat(agentPerformances.getStartDate().compareTo(startDate), is(0));
        assertThat(agentPerformances.getEndDate().compareTo(endDate), is(0));
        assertThat(agentPerformances.getAgents().length, is(2));
        assertThat(agentPerformances.getAgents()[0].getAgent(), is("Agent 1"));
        assertThat(agentPerformances.getAgents()[0].getSuccess(), is(1));
        assertThat(agentPerformances.getAgents()[0].getFail(), is(1));
        assertThat(agentPerformances.getAgents()[0].getWithdrawn(), is(1));
        assertThat(agentPerformances.getAgents()[0].getTotal(), is(1));
    }
}
