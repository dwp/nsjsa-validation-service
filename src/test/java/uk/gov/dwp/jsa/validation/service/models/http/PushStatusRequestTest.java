package uk.gov.dwp.jsa.validation.service.models.http;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PushStatusRequestTest {

    public static final LocalDateTime CREATED_TIMESTAMP = LocalDateTime.now();
    public static final String AGENT = "AGENT";
    public static final PushStatusType PUSH_STATUS_TYPE = PushStatusType.PUSH_FAILED;
    public static final UUID BOOKING_STATUS_ID = UUID.randomUUID();
    private PushStatusRequest request;

    @Test
    public void hasDefaultConstructor() {
        new PushStatusRequest();
    }

    @Test
    public void constructorSetsFields() {
        request = new PushStatusRequest(
                BOOKING_STATUS_ID,
                PUSH_STATUS_TYPE,
                AGENT,
                CREATED_TIMESTAMP);
        assertThat(BOOKING_STATUS_ID, is(request.getBookingStatusId()));
        assertThat(PUSH_STATUS_TYPE, is(request.getStatus()));
        assertThat(AGENT, is(request.getAgent()));
        assertThat(CREATED_TIMESTAMP, is(request.getCreatedTimestamp()));
    }

    @Test
    public void setsPushStatusType() {
        request = new PushStatusRequest();
        request.setStatus(PUSH_STATUS_TYPE);
        assertThat(PUSH_STATUS_TYPE, is(request.getStatus()));
    }

    @Test
    public void setsBookingStatusId() {
        request = new PushStatusRequest();
        request.setBookingStatusId(BOOKING_STATUS_ID);
        assertThat(BOOKING_STATUS_ID, is(request.getBookingStatusId()));
    }

    @Test
    public void setsAgent() {
        request = new PushStatusRequest();
        request.setAgent(AGENT);
        assertThat(AGENT, is(request.getAgent()));
    }

    @Test
    public void setsCreatedTimeStamp() {
        request = new PushStatusRequest();
        request.setCreatedTimestamp(CREATED_TIMESTAMP);
        assertThat(CREATED_TIMESTAMP, is(request.getCreatedTimestamp()));
    }


}
