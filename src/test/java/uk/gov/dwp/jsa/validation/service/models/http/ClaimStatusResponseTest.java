package uk.gov.dwp.jsa.validation.service.models.http;

import org.junit.Test;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ClaimStatusResponseTest {

    private static final String AGENT = "AGENT";
    private static final String NULL_AGENT = null;
    public static final BookingStatusResponse AGENT_NEW_CLAIM_BOOKING_STATUS =
            new BookingStatusResponseBuilder()
                    .withBookingStatusType(BookingStatusType.NEW_CLAIM)
                    .withAgent(AGENT)
                    .build();
    public static final BookingStatusResponse CITIZEN_NEW_CLAIM_BOOKING_STATUS =
            new BookingStatusResponseBuilder()
                    .withBookingStatusType(BookingStatusType.NEW_CLAIM)
                    .withAgent(NULL_AGENT)
                    .build();

    private ClaimStatusResponse response;
    private boolean isClaimCreatedByAgent;


    @Test
    public void claimIsCreatedByAgent() {
        givenAResponseCreatedByAgent();
        whenICheckWhoCreatedClaim();
        thenTheClaimIsCreatedByAgent();
    }

    @Test
    public void claimIsCreatedByCitizen() {
        givenAResponseCreatedByCitizen();
        whenICheckWhoCreatedClaim();
        thenTheClaimIsCreatedByCitizen();
    }

    private void givenAResponseCreatedByCitizen() {
        response = new ClaimStatusResponse();
        response.setBookingStatuses(Arrays.asList(CITIZEN_NEW_CLAIM_BOOKING_STATUS));
    }

    private void givenAResponseCreatedByAgent() {
        response = new ClaimStatusResponse();
        response.setBookingStatuses(Arrays.asList(AGENT_NEW_CLAIM_BOOKING_STATUS));
    }

    private void whenICheckWhoCreatedClaim() {
        isClaimCreatedByAgent = response.isClaimCreatedByAgent();
    }

    private void thenTheClaimIsCreatedByAgent() {
        assertThat(isClaimCreatedByAgent, is(true));
    }

    private void thenTheClaimIsCreatedByCitizen() {
        assertThat(isClaimCreatedByAgent, is(false));
    }
}
