package uk.gov.dwp.jsa.validation.service.gateway;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.dwp.jsa.adaptors.HttpEntityFactory;
import uk.gov.dwp.jsa.adaptors.RestfulExecutor;
import uk.gov.dwp.jsa.adaptors.ServicesProperties;
import uk.gov.dwp.jsa.validation.service.models.http.CurrentStatusDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantServiceClientTest {

    private static final UUID CLAIMANT_ID = UUID.randomUUID();
    private static final String EXPECTED_URI = format("%s/nsjsa/v%s/citizen/%s/status","localhost", 1, CLAIMANT_ID);


    @Mock
    private RestfulExecutor restfulExecutor;

    @Mock
    private HttpEntityFactory entityFactory;

    @Mock
    private ServicesProperties servicesProperties;

    private ClaimantServiceClient sut;

    @Before
    public void setUp() {
        sut = new ClaimantServiceClient( restfulExecutor,servicesProperties);
        when(servicesProperties.getClaimantServer()).thenReturn("localhost");
        when(servicesProperties.getClaimantVersion()).thenReturn("1");

    }

    @Test(expected = NullPointerException.class)
    public void assertThatClaimantIdIsNotNull() {
        sut.sendLatestStatus(null, new CurrentStatusDto());
    }

    @Test(expected = NullPointerException.class)
    public void assertThatCurrentStatusDtoIsNotNull() {
        sut.sendLatestStatus(CLAIMANT_ID, null);
    }

    @Test(expected = NullPointerException.class)
    public void assertThatCurrentStatusCreatedTimestampIsNotNull() {
        final CurrentStatusDto currentStatusDto = new CurrentStatusDto();
        currentStatusDto.setBookingStatus(new CurrentStatusDto.StatusDto("status", "substatus"));
        currentStatusDto.setPushStatus(new CurrentStatusDto.StatusDto("status", "substatus"));

        sut.sendLatestStatus(CLAIMANT_ID, currentStatusDto);
    }

    @Test
    public void assertThatClientCallsClaimantServiceWithCorrectData() {
        final CurrentStatusDto currentStatusDto = new CurrentStatusDto();
        currentStatusDto.setBookingStatus(new CurrentStatusDto.StatusDto("status1", "substatus1"));
        currentStatusDto.setPushStatus(new CurrentStatusDto.StatusDto("status2", "substatus2"));
        currentStatusDto.setCreatedTimestamp(LocalDateTime.of(2018, Month.DECEMBER, 1, 1, 1));

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CurrentStatusDto> currentStatusDtoCaptor =
                ArgumentCaptor.forClass(CurrentStatusDto.class);

        sut.sendLatestStatus(CLAIMANT_ID, currentStatusDto);

        verify(restfulExecutor).put(uriCaptor.capture(), currentStatusDtoCaptor.capture(), any(), any());

        assertThat(uriCaptor.getValue(), Is.is(EXPECTED_URI));
        assertThat(currentStatusDtoCaptor.getValue().getCreatedTimestamp(),
                Is.is(currentStatusDto.getCreatedTimestamp()));
        assertThat(currentStatusDtoCaptor.getValue().getBookingStatus().getStatus(), Is.is(currentStatusDto.getBookingStatus().getStatus()));
        assertThat(currentStatusDtoCaptor.getValue().getBookingStatus().getSubstatus(), Is.is(currentStatusDto.getBookingStatus().getSubstatus()));
        assertThat(currentStatusDtoCaptor.getValue().getPushStatus().getStatus(), Is.is(currentStatusDto.getPushStatus().getStatus()));
        assertThat(currentStatusDtoCaptor.getValue().getPushStatus().getSubstatus(), Is.is(currentStatusDto.getPushStatus().getSubstatus()));

    }

}
