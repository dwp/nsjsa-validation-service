package uk.gov.dwp.jsa.validation.service.util;

import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusResponse;

import java.io.Serializable;
import java.util.Comparator;

public class BookingStatusComparator implements Comparator<BookingStatusResponse>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final BookingStatusResponse bookingStatus1, final BookingStatusResponse bookingStatus2) {
        return bookingStatus2.getCreatedTimestamp().compareTo(bookingStatus1.getCreatedTimestamp());
    }
}
