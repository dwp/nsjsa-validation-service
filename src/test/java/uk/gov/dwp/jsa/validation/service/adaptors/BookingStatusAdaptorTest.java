package uk.gov.dwp.jsa.validation.service.adaptors;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusResponse;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BookingStatusAdaptorTest {

    @Test
    public void fromRequest_requestPassedIn_bookingStatusReturned() {
        BookingStatusRequest bookingStatusRequest = new BookingStatusRequest(
                UUID.randomUUID(),
                BookingStatusType.NEW_CLAIM,
                "substatus",
                "job centre",
                "agent",
                LocalDateTime.now()
        );

        BookingStatus bookingStatus = BookingStatusAdaptor.fromRequest(bookingStatusRequest);

        String hash = DigestUtils.sha256Hex("NEW CLAIMsubstatusjob centreagent");

        assertThat(bookingStatus.getStatus(), is(equalTo("NEW_CLAIM")));
        assertThat(bookingStatus.getSubstatus(), is(equalTo("substatus")));
        assertThat(bookingStatus.getJobCentreCode(), is(equalTo("job centre")));
        assertThat(bookingStatus.getAgent(), is(equalTo("agent")));
        assertThat(bookingStatus.getHash(), is(equalTo("a25c5694ff1a1d71eac5174f54e5addc5c5ce0dadda4c86a6a793407e36f0fc4")));
    }

    @Test
    public void toResponse_bookingStatusPassedIn_responseReturned() {
        BookingStatus bookingStatus = new BookingStatus(
                "NEW_CLAIM",
                "substatus",
                "job centre",
                "agent",
                "a25c5694ff1a1d71eac5174f54e5addc5c5ce0dadda4c86a6a793407e36f0fc4"
        );

        BookingStatusResponse bookingStatusResponse = BookingStatusAdaptor.toResponse(bookingStatus);

        assertThat(bookingStatusResponse.getStatus(), is(equalTo(BookingStatusType.NEW_CLAIM)));
        assertThat(bookingStatusResponse.getSubstatus(), is(equalTo("substatus")));
        assertThat(bookingStatusResponse.getJobCentreCode(), is(equalTo("job centre")));
        assertThat(bookingStatusResponse.getAgent(), is(equalTo("agent")));
        assertThat(bookingStatusResponse.getCreatedTimestamp(), is(equalTo(bookingStatus.getCreatedTimestamp())));
    }

}
