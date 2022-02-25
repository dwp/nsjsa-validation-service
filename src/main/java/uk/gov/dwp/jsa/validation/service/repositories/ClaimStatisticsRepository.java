package uk.gov.dwp.jsa.validation.service.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformance;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Component
public class ClaimStatisticsRepository {

    private final EntityManagerFactory emf;
    private final BookingStatusRepository bookingStatusRepository;

    private static final int SIZE = 11;
    private static final int DAILY_TOTAL = 0;
    private static final int HEAD_OF_WORK = 1;
    private static final int OLDEST_CLAIM = 2;
    private static final int DAILY_CLOSED = 3;
    private static final int CLOSED_24HR = 4;
    private static final int CLOSED_48HR = 5;
    private static final int WTD_TOTAL = 6;
    private static final int WTD_CLOSED_24HR = 7;
    private static final int WTD_CLOSED_48HR = 8;
    private static final int OUTSIDE_24HR = 9;
    private static final int OUTSIDE_48HR = 10;
    private static final int AGENT = 0;
    private static final int SUCCESS = 1;
    private static final int FAIL = 2;
    private static final int WITHDRAWN = 3;
    private static final int TOTAL = 4;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimStatisticsRepository.class);

    @Value("${validation.db.schema:validation_schema}")
    private String dbSchema;

    @Autowired
    public ClaimStatisticsRepository(final EntityManagerFactory entityManagerFactory,
                                     final BookingStatusRepository bookingStatusRepository) {
        this.emf = entityManagerFactory;
        this.bookingStatusRepository = bookingStatusRepository;
    }

    private Object querySingleResult(final String sql) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createNativeQuery(sql);
            Object[] results = query.getResultList().toArray();
            if (results.length == 0) {
                return new Object[SIZE];
            } else {
                return results[0];
            }
        } finally {
            em.close();
        }
    }

    private List<Object> queryListResult(final String sql) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createNativeQuery(sql);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Gets the claim statistics from the validation service database using a stored procedure.
     *
     * @param date date to get the statistics for
     *
     * @return claim statistics from the validation service database, excluding assisted digital claim count
     */
    public ClaimStatistics getAllClaimStatistics(final LocalDate date) {
        String allStatsFormat = "SELECT%n"
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
                + "FROM " + dbSchema + "." + "claim_statistics('%s')";

        String allStats = format(allStatsFormat, date);

        LOGGER.debug("Calling stored procedure to get claim statistics");
        Object results = querySingleResult(allStats);
        Object[] counts = (Object[]) results;

        LOGGER.debug("Calling repository to get assisted digital claim count");
        final int assistedDigitalClaimCount = bookingStatusRepository.getAssistedDigitalClaimCount(
                LocalDateTime.of(date, LocalTime.MIN),
                LocalDateTime.of(date, LocalTime.MAX)
        );

        return new ClaimStatistics(
                counts[DAILY_TOTAL] != null ? (Integer) counts[DAILY_TOTAL] : 0,
                counts[HEAD_OF_WORK] != null ? (Integer) counts[HEAD_OF_WORK] : 0,
                counts[OLDEST_CLAIM] != null ? ((Timestamp) counts[OLDEST_CLAIM]).toLocalDateTime() : null,
                counts[DAILY_CLOSED] != null ? (Integer) counts[DAILY_CLOSED] : 0,
                counts[CLOSED_24HR] != null ? (Double) counts[CLOSED_24HR] : 0,
                counts[CLOSED_48HR] != null ? (Double) counts[CLOSED_48HR] : 0,
                counts[WTD_TOTAL] != null ? (Integer) counts[WTD_TOTAL] : 0,
                counts[WTD_CLOSED_24HR] != null ? (Double) counts[WTD_CLOSED_24HR] : 0,
                counts[WTD_CLOSED_48HR] != null ? (Double) counts[WTD_CLOSED_48HR] : 0,
                counts[OUTSIDE_24HR] != null ? (Integer) counts[OUTSIDE_24HR] : 0,
                counts[OUTSIDE_48HR] != null ? (Integer) counts[OUTSIDE_48HR] : 0,
                assistedDigitalClaimCount
        );
    }

    public AgentPerformances getAgentPerformances(final LocalDate from, final LocalDate to) {
        String agentStatsFormat = "SELECT%n"
                + "\tagent, success, fail, withdrawn, total%n"
                + "FROM " + dbSchema + "." + "agent_performance('%s', '%s');";

        String agentStats = format(agentStatsFormat, from, to);

        List<Object> rows = queryListResult(agentStats);
        List<AgentPerformance> agentPerformanceList = new ArrayList<>();

        for (Object row : rows) {
            Object[] rowData = (Object[]) row;

            AgentPerformance ap = new AgentPerformance(
                    (String) rowData[AGENT],
                    (Integer) rowData[SUCCESS],
                    (Integer) rowData[FAIL],
                    (Integer) rowData[WITHDRAWN],
                    (Integer) rowData[TOTAL]
            );
            agentPerformanceList.add(ap);
        }

        return new AgentPerformances(from, to, agentPerformanceList.toArray(new AgentPerformance[0]));
    }
}
