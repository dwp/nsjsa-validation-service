package uk.gov.dwp.jsa.validation.service.adaptors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusResponse;

@Component
public final class BookingStatusAdaptor {

    private BookingStatusAdaptor() {
    }

    public static BookingStatus fromRequest(final BookingStatusRequest bookingStatusRequest) {
        String hash = DigestUtils.sha256Hex(
                bookingStatusRequest.getStatus().name()
                        + bookingStatusRequest.getSubstatus()
                        + bookingStatusRequest.getJobCentreCode()
                        + bookingStatusRequest.getAgent()
        );

        return new BookingStatus(
                bookingStatusRequest.getStatus().name(),
                bookingStatusRequest.getSubstatus(),
                bookingStatusRequest.getJobCentreCode(),
                bookingStatusRequest.getAgent(),
                hash
        );
    }

    public static BookingStatusResponse toResponse(final BookingStatus bookingStatus) {
        return new BookingStatusResponse(bookingStatus.getBookingStatusId(),
                BookingStatusType.valueOf(bookingStatus.getStatus()),
                bookingStatus.getSubstatus(),
                bookingStatus.getJobCentreCode(),
                bookingStatus.getAgent(),
                bookingStatus.getCreatedTimestamp()
        );
    }
}
