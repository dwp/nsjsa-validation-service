package uk.gov.dwp.jsa.validation.service.integration;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.cache.ElastiCacheAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextCredentialsAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextResourceLoaderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.jdbc.AmazonRdsDatabaseAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.metrics.CloudWatchExportAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.validation.service.repositories.BookingStatusRepository;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatisticsRepository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DbConfig.class})
@EnableAutoConfiguration(exclude = {
        ContextInstanceDataAutoConfiguration.class,
        ContextCredentialsAutoConfiguration.class,
        ContextRegionProviderAutoConfiguration.class,
        ContextResourceLoaderAutoConfiguration.class,
        ContextStackAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        ElastiCacheAutoConfiguration.class,
        MessagingAutoConfiguration.class,
        AmazonRdsDatabaseAutoConfiguration.class,
        CloudWatchExportAutoConfiguration.class
})
public class ClaimStatisticsIntegrationTest {

    @Autowired
    DataSource dataSource;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer(
            "postgres:10.3")
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("pass");

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    private BookingStatusRepository bookingStatusRepository;

    ClaimStatisticsRepository claimStatisticsRepository;
    static ClaimStatistics claimStatistics;

    @Before
    public void setup() throws SQLException {
        // simulate before all due to it's limitations
        if (claimStatistics == null) {
            // The Sql annotation doesn't support a 'before all' mode, so we simulate it
            try (Connection conn = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("data/TestData.sql"));
            }
            // Also the context of 'before all' wouldn't allow the below to work, so we simulate it
            claimStatisticsRepository = new ClaimStatisticsRepository(entityManagerFactory, bookingStatusRepository);
            int dayOffset = LocalDate.now().getDayOfWeek().getValue() - 1;
            LocalDate mondayDate = LocalDate.now().minusDays(dayOffset);
            claimStatistics = claimStatisticsRepository.getAllClaimStatistics(mondayDate);
        }
    }

    @Test
    public void contextLoads() throws Exception {
//        if (true) throw new Exception();
        assertThat(claimStatistics, is(notNullValue()));
    }

    @Test
    public void casesRecievedInDay() {
        assertThat(claimStatistics.getCasesReceivedInDay(), is(3));
    }

    @Test
    public void headOfWork() {
        assertThat(claimStatistics.getHeadOfWork(), is(8));
    }

    @Test
    public void oldestOpenClaim() {
        int dayOffset = LocalDate.now().getDayOfWeek().getValue() - 1;
        LocalDateTime expectedOldestClaimTime = LocalDate.now().minusDays(dayOffset + 10).atTime(16,0);
        assertThat(claimStatistics.getOldestClaimOpen(), is(expectedOldestClaimTime));
    }

    @Test
    public void casesClearedInDay() {
        assertThat(claimStatistics.getCasesClearedInDay(), is(5));
    }

    @Test
    public void percentageOfClaimsInDayClosedIn24hr() {
        assertThat(round(claimStatistics.getPercentageOfClaimsInDayClosedIn24hr()), is(round(40.00)));
    }

    @Test
    public void percentageOfClaimsInDayClosedIn48hr() {
        assertThat(round(claimStatistics.getPercentageOfClaimsInDayClosedIn48hr()), is(round(60.00)));
    }

    @Test
    public void totalNumberOfClaimsInWeek() {
        assertThat(claimStatistics.getTotalNumberOfClaimsInWeek(), is(6));
    }

    @Test
    public void percentageOfClaimsInWeekClosedIn24hr() {
        assertThat(round(claimStatistics.getPercentageOfClaimsInWeekClosedIn24hr()), is(round(33.33)));
    }

    @Test
    public void percentageOfClaimsInWeekClosedIn48hr() {
        assertThat(round(claimStatistics.getPercentageOfClaimsInWeekClosedIn48hr()), is(round(66.67)));
    }

    @Test
    public void casesOutstandingOutside24hrKpi() {
        assertThat(claimStatistics.getCasesOutstandingOutside24hrKpi(), is(5));
    }

    @Test
    public void casesOutstandingOutside48hrKpi() {
        assertThat(claimStatistics.getCasesOutstandingOutside48hrKpi(), is(2));
    }

    private BigDecimal round(double value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
