package uk.gov.dwp.jsa.validation.service.util;

import org.junit.Test;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusResponse;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BookingStatusComparatorTest {

    private BookingStatusComparator bookingStatusComparator = new BookingStatusComparator();

    @Test
    public void compareReturnsNegative() {
        BookingStatusResponse bookingStatusResponse1 = new BookingStatusResponse();
        bookingStatusResponse1.setCreatedTimestamp(LocalDateTime.now().minusDays(2));
        BookingStatusResponse bookingStatusResponse2 = new BookingStatusResponse();
        bookingStatusResponse2.setCreatedTimestamp(LocalDateTime.now());

        int compare = bookingStatusComparator.compare(bookingStatusResponse2, bookingStatusResponse1);

        assertThat(compare < 0, is(true));
    }

    @Test
    public void compareReturnsZeroForEqualTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        BookingStatusResponse bookingStatusResponse1 = new BookingStatusResponse();
        bookingStatusResponse1.setCreatedTimestamp(now);
        BookingStatusResponse bookingStatusResponse2 = new BookingStatusResponse();
        bookingStatusResponse2.setCreatedTimestamp(now);

        int compare = bookingStatusComparator.compare(bookingStatusResponse1, bookingStatusResponse2);

        assertThat(compare == 0, is(true));
    }
}
