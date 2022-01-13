package uk.gov.dwp.jsa.validation.service.models.http;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClaimStatusRequestTest {

    private ClaimStatusResponse claimStatusResponse = new ClaimStatusResponse();

    @Test
    public void latestBookingIsReturnedAsExpected() {
        List<BookingStatusResponse> bookingStatuses = new ArrayList<>();
        getBookingStatusResponse(bookingStatuses, 3);
        getBookingStatusResponse(bookingStatuses, 0);
        BookingStatusResponse bookingStatusResponse = getBookingStatusResponse(bookingStatuses, -3);


        claimStatusResponse.setBookingStatuses(bookingStatuses);

        assertThat(claimStatusResponse.latestBookingStatus(), is(bookingStatusResponse));
    }

    @Test
    public void latestPushStatusIsReturnedAsExpected() {
        List<PushStatusResponse> pushStatuses = new ArrayList<>();
        getPushStatusReq1(pushStatuses, 3);
        getPushStatusReq1(pushStatuses, 0);
        PushStatusResponse pushStatusResponse = getPushStatusReq1(pushStatuses, -3);

        claimStatusResponse.setPushStatuses(pushStatuses);

        assertThat(claimStatusResponse.latestPushStatus(), is(pushStatusResponse));
    }

    private BookingStatusResponse getBookingStatusResponse(final List<BookingStatusResponse> bookingStatuses,
                                                           final int daysOffset) {
        BookingStatusResponse bookingStatusResponse = new BookingStatusResponse();
        bookingStatusResponse.setCreatedTimestamp(LocalDateTime.now().minusDays(daysOffset));
        bookingStatuses.add(bookingStatusResponse);
        return bookingStatusResponse;
    }

    private PushStatusResponse getPushStatusReq1(final List<PushStatusResponse> pushStatuses,
                                                 final int daysOffset) {
        PushStatusResponse pushStatusResponse = new PushStatusResponse();
        pushStatusResponse.setCreatedTimestamp(LocalDateTime.now().minusDays(daysOffset));
        pushStatuses.add(pushStatusResponse);
        return pushStatusResponse;
    }

}
