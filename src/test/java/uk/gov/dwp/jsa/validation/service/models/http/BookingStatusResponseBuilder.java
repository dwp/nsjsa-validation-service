package uk.gov.dwp.jsa.validation.service.models.http;

import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;

public class BookingStatusResponseBuilder {
    public static final String AGENT = "AGENT";
    public static BookingStatusType BOOKING_STATUS_TYPE = BookingStatusType.NEW_CLAIM;

    private String agent = AGENT;
    private BookingStatusType bookingStatusType = BOOKING_STATUS_TYPE;

    public BookingStatusResponse build() {
        final BookingStatusResponse bookingStatusResponse = new BookingStatusResponse();
        bookingStatusResponse.setAgent(agent);
        bookingStatusResponse.setStatus(bookingStatusType);
        return bookingStatusResponse;
    }

    public BookingStatusResponseBuilder withAgent(String agent) {
        this.agent = agent;
        return this;
    }

    public BookingStatusResponseBuilder withBookingStatusType(BookingStatusType bookingStatusType) {
        this.bookingStatusType = bookingStatusType;
        return this;
    }
}

