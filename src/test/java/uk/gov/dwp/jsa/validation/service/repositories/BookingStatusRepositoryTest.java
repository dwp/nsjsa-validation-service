package uk.gov.dwp.jsa.validation.service.repositories;


import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(Parameterized.class)
@DataJpaTest
@ActiveProfiles("embedded-db")
@Import(BookingStatusRepositoryTest.EmbeddedDatabaseTestConfiguration.class)
@Sql("classpath:/assisted-digital-test-data.sql")
public class BookingStatusRepositoryTest {

    @ClassRule
    public static final SpringClassRule scr = new SpringClassRule();

    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    @Autowired
    private BookingStatusRepository bookingStatusRepository;

    @Parameter(0)
    public LocalDate currentTestDate;

    @Parameter(1)
    public int expected;

    private static final LocalDate DATE_ONE = LocalDate.of(2021, 12, 20);
    private static final LocalDate DATE_TWO = LocalDate.of(2021, 12, 21);
    private static final LocalDate DATE_THREE = LocalDate.of(2021, 12, 22);
    private static final int DATE_ONE_EXPECTED = 1;
    private static final int DATE_TWO_EXPECTED = 3;
    private static final int DATE_THREE_EXPECTED = 0;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { DATE_ONE, DATE_ONE_EXPECTED }, { DATE_TWO, DATE_TWO_EXPECTED }, { DATE_THREE, DATE_THREE_EXPECTED }
        });
    }

    @Test
    public void testGetAssistedDigitalCount() {
        final LocalDateTime start = LocalDateTime.of(currentTestDate, LocalTime.MIN);
        final LocalDateTime end = LocalDateTime.of(currentTestDate, LocalTime.MAX);
        assertThat(bookingStatusRepository.getAssistedDigitalClaimCount(start, end)).isEqualTo(expected);
    }

    /**
     * {@link uk.gov.dwp.jsa.validation.service.controllers.CustomErrorController} still gets created for some reason
     * and requires a bean of {@link ErrorAttributes} this config keeps spring initialisation happy
     */
    @TestConfiguration
    public static class EmbeddedDatabaseTestConfiguration {
        @Bean
        public ErrorAttributes errorAttributes() {
            return new DefaultErrorAttributes();
        }
    }
}
