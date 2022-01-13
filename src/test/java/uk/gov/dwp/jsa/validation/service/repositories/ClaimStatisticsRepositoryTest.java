package uk.gov.dwp.jsa.validation.service.repositories;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformance;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClaimStatisticsRepositoryTest {
    private static final LocalDate startDate = LocalDate.now();
    private static final LocalDate endDate = startDate.plusDays(3);

    String allStats = format("SELECT%n"
            + "\tdaily_total,%n"
            + "\topen_total,%n"
            + "\toldest_open_claim,%n"
            + "\tdaily_closed,%n"
            + "\tclosed_24hr,%n"
            + "\tclosed_48hr,%n"
            + "\twtd_total,%n"
            + "\twtd_closed_24hr,%n"
            + "\twtd_closed_48hr,%n"
            + "\toutside_24hr,%n"
            + "\toutside_48hr%n"
            + "FROM validation_schema.claim_statistics('%s')", startDate);

    @Mock
    EntityManagerFactory entityManagerFactory;

    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    ClaimStatisticsRepository claimStatisticsRepository;

    @Before
    public void setUp() {
        initMocks(this);
    }

    private ClaimStatisticsRepository createRepo(EntityManagerFactory entityManagerFactory) {
        ClaimStatisticsRepository claimStatisticsRepository = new ClaimStatisticsRepository(entityManagerFactory);
        ReflectionTestUtils.setField(claimStatisticsRepository, "dbSchema", "validation_schema");
        return claimStatisticsRepository;
    }

    @Test
    public void getAllClaimStatistics() {

        int typedCount = 3;
        double typedPercentage = 3d;
        Object count = typedCount;
        Object percentage = typedPercentage;
        LocalDateTime typedOldest = LocalDateTime.parse("2019-05-03T10:11:10");
        Object oldest = Timestamp.valueOf(typedOldest);

        Object result = new Object[] { count, count, oldest, count, percentage, percentage, count, percentage, percentage, count, count };
        List<Object> resultList = new ArrayList<Object>() {{ add(result); }};

        when(query.getResultList()).thenReturn(resultList);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(allStats)).thenReturn(query);

        claimStatisticsRepository = createRepo(entityManagerFactory);
        ClaimStatistics actual = claimStatisticsRepository.getAllClaimStatistics(startDate);
        ClaimStatistics expected = new ClaimStatistics(
            typedCount, typedCount, typedOldest, typedCount, typedPercentage, typedPercentage, typedCount, typedPercentage, typedPercentage, typedCount, typedCount
        );

        assert(expected.getCasesReceivedInDay() == actual.getCasesReceivedInDay());
        assert(expected.getHeadOfWork() == actual.getHeadOfWork());
        assert(expected.getOldestClaimOpen().compareTo(actual.getOldestClaimOpen()) == 0);
        assert(expected.getCasesClearedInDay() == actual.getCasesClearedInDay());
        assert(expected.getPercentageOfClaimsInDayClosedIn24hr() == actual.getPercentageOfClaimsInDayClosedIn24hr());
        assert(expected.getPercentageOfClaimsInDayClosedIn48hr() == actual.getPercentageOfClaimsInDayClosedIn48hr());
        assert(expected.getTotalNumberOfClaimsInWeek() == actual.getTotalNumberOfClaimsInWeek());
        assert(expected.getPercentageOfClaimsInWeekClosedIn24hr() == actual.getPercentageOfClaimsInWeekClosedIn24hr());
        assert(expected.getPercentageOfClaimsInWeekClosedIn48hr() == actual.getPercentageOfClaimsInWeekClosedIn48hr());
        assert(expected.getCasesOutstandingOutside24hrKpi() == actual.getCasesOutstandingOutside24hrKpi());
        assert(expected.getCasesOutstandingOutside48hrKpi() == actual.getCasesOutstandingOutside48hrKpi());
    }

    @Test
    public void getAllClaimStatisticsNoneExist() {
        List<Object> resultList = new ArrayList<Object>() {};

        when(query.getResultList()).thenReturn(resultList);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(allStats)).thenReturn(query);

        claimStatisticsRepository = createRepo(entityManagerFactory);
        ClaimStatistics actual = claimStatisticsRepository.getAllClaimStatistics(startDate);
        ClaimStatistics expected = new ClaimStatistics(
                0, 0, null, 0, 0d, 0d, 0, 0d, 0d, 0, 0
        );

        assert(expected.getCasesReceivedInDay() == actual.getCasesReceivedInDay());
        assert(expected.getHeadOfWork() == actual.getHeadOfWork());
        assert(actual.getOldestClaimOpen() == null);
        assert(expected.getCasesClearedInDay() == actual.getCasesClearedInDay());
        assert(expected.getPercentageOfClaimsInDayClosedIn24hr() == actual.getPercentageOfClaimsInDayClosedIn24hr());
        assert(expected.getPercentageOfClaimsInDayClosedIn48hr() == actual.getPercentageOfClaimsInDayClosedIn48hr());
        assert(expected.getTotalNumberOfClaimsInWeek() == actual.getTotalNumberOfClaimsInWeek());
        assert(expected.getPercentageOfClaimsInWeekClosedIn24hr() == actual.getPercentageOfClaimsInWeekClosedIn24hr());
        assert(expected.getPercentageOfClaimsInWeekClosedIn48hr() == actual.getPercentageOfClaimsInWeekClosedIn48hr());
        assert(expected.getCasesOutstandingOutside24hrKpi() == actual.getCasesOutstandingOutside24hrKpi());
        assert(expected.getCasesOutstandingOutside48hrKpi() == actual.getCasesOutstandingOutside48hrKpi());
    }

    @Test
    public void getAgentPerformances() {
        String agentStats = format("SELECT%n" +
                "\tagent, success, fail, withdrawn, total%n" +
                "FROM validation_schema.agent_performance('%s', '%s');", startDate, endDate);

        int typedCount = 3;
        Object count = typedCount;

        Object mockAgent1 = new Object[] { "Agent 1", count, count, count, count };
        Object mockAgent2 = new Object[] { "Agent 2", count, count, count, count };
        List<Object> resultList = new ArrayList<Object>() {{ add(mockAgent1); add(mockAgent2); }};

        when(query.getResultList()).thenReturn(resultList);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(agentStats)).thenReturn(query);

        claimStatisticsRepository = createRepo(entityManagerFactory);
        AgentPerformances actual = claimStatisticsRepository.getAgentPerformances(startDate, endDate);
        AgentPerformances expected = new AgentPerformances(startDate, endDate,
                new AgentPerformance[] {
                new AgentPerformance("Agent 1", 3, 3, 3,3),
                new AgentPerformance("Agent 2", 3, 3, 3,3)
            }
        );

        assert(expected.getAgents().length == actual.getAgents().length);
        assert(expected.getAgents()[0].getAgent() == actual.getAgents()[0].getAgent());
        assert(expected.getAgents()[0].getSuccess() == actual.getAgents()[0].getSuccess());
        assert(expected.getAgents()[0].getFail() == actual.getAgents()[0].getFail());
        assert(expected.getAgents()[0].getWithdrawn() == actual.getAgents()[0].getWithdrawn());
        assert(expected.getAgents()[0].getTotal() == actual.getAgents()[0].getTotal());
    }
}
