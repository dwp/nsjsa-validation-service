package uk.gov.dwp.jsa.validation.service.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformance;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
    private static final LocalDate START_DATE = LocalDate.now();
    private static final LocalDate END_DATE = START_DATE.plusDays(3);
    private static final int ASSISTED_DIGITAL_CLAIM_COUNT = 50;
    final AgentPerformance[] AGENT_PERFORMANCES = new AgentPerformance[] {
            new AgentPerformance("Agent 1", 1,1,1,1),
            new AgentPerformance("Agent 2", 1,1,1,1)
    };

    @Mock
    private ClaimStatisticsRepository mockClaimStatisticsRepository;

    @InjectMocks
    private ClaimStatisticsService subjectUnderTest;

    @Before
    public void beforeEachTest() {
        //Recreate agent performance in case tests have altered it

    }

    @Test
    public void testGetClaimStatisticsWithPopulatedOptionalAssistedDigital() {
        //Arrange
        final ClaimStatistics expected = new ClaimStatistics(
                CASES_RECEIVED_IN_DAY,
                HEAD_OF_WORK,
                OLDEST_CLAIM_OPEN,
                CASES_CLEARED_IN_DAY,
                PERCENTAGE_OF_CLAIMS_IN_DAY_CLOSED_IN_24_HR,
                PERCENTAGE_OF_CLAIMS_IN_DAY_CLOSED_IN_48_HR,
                TOTAL_NUMBER_OF_CLAIMS_IN_WEEK,
                PERCENTAGE_OF_CLAIMS_IN_WEEK_CLOSED_IN_24_HR,
                PERCENTAGE_OF_CLAIMS_IN_WEEK_CLOSED_IN_48_HR,
                OUTSTANDING_OUTSIDE_24HR,
                OUTSTANDING_OUTSIDE_48HR,
                ASSISTED_DIGITAL_CLAIM_COUNT
        );
        when(mockClaimStatisticsRepository.getAllClaimStatistics(START_DATE)).thenReturn(expected);

        //Act
        final ClaimStatistics actual = subjectUnderTest.getAllClaimStatistics(START_DATE);

        //Assert
        assertClaimStatistics(actual, expected);
    }

    @Test
    public void getAgentPerformances() {
        //Arrange
        final AgentPerformances expected = new AgentPerformances(START_DATE, END_DATE, AGENT_PERFORMANCES);
        when(mockClaimStatisticsRepository.getAgentPerformances(START_DATE, START_DATE)).thenReturn(expected);

        //Act
        final AgentPerformances actual = subjectUnderTest.getAgentPerformances(START_DATE, START_DATE);

        //Assert
        assertAgentPerformances(actual, expected);
    }

    private void assertClaimStatistics(final ClaimStatistics actual, final ClaimStatistics expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getCasesReceivedInDay()).isEqualTo(expected.getCasesReceivedInDay());
        assertThat(actual.getHeadOfWork()).isEqualTo(expected.getHeadOfWork());
        assertThat(actual.getOldestClaimOpen()).isEqualTo(expected.getOldestClaimOpen());
        assertThat(actual.getCasesClearedInDay()).isEqualTo(expected.getCasesClearedInDay());
        assertThat(actual.getPercentageOfClaimsInDayClosedIn24hr()).isEqualTo(expected.getPercentageOfClaimsInDayClosedIn24hr());
        assertThat(actual.getPercentageOfClaimsInDayClosedIn48hr()).isEqualTo(expected.getPercentageOfClaimsInDayClosedIn48hr());
        assertThat(actual.getTotalNumberOfClaimsInWeek()).isEqualTo(expected.getTotalNumberOfClaimsInWeek());
        assertThat(actual.getPercentageOfClaimsInWeekClosedIn24hr()).isEqualTo(expected.getPercentageOfClaimsInWeekClosedIn24hr());
        assertThat(actual.getPercentageOfClaimsInWeekClosedIn48hr()).isEqualTo(expected.getPercentageOfClaimsInWeekClosedIn48hr());
        assertThat(actual.getCasesOutstandingOutside24hrKpi()).isEqualTo(expected.getCasesOutstandingOutside24hrKpi());
        assertThat(actual.getCasesOutstandingOutside48hrKpi()).isEqualTo(expected.getCasesOutstandingOutside48hrKpi());
        assertThat(actual.getAssistedDigitalClaimCount()).isEqualTo(expected.getAssistedDigitalClaimCount());
    }

    private void assertAgentPerformances(final AgentPerformances actual, final AgentPerformances expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getStartDate()).isEqualTo(expected.getStartDate());
        assertThat(actual.getEndDate()).isEqualTo(expected.getEndDate());
        assertThat(actual.getAgents()).containsOnly(expected.getAgents());
    }
}
