package uk.gov.dwp.jsa.validation.service.adaptors;

import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;
import uk.gov.dwp.jsa.validation.service.models.db.ClaimStatus;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusResponse;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusResponse;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusType;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class ClaimStatusAdaptor {

    public ClaimStatusResponse toResponse(final ClaimStatus claimStatus) {
        ClaimStatusResponse claimStatusResponse = new ClaimStatusResponse();
        claimStatusResponse.setClaimantId(claimStatus.getClaimantId());
        claimStatusResponse.setLocked(claimStatus.isLocked());
        claimStatusResponse.setCreatedTimestamp(claimStatus.getCreatedTimestamp());
        claimStatusResponse.setUpdatedTimestamp(claimStatus.getUpdatedTimestamp());

        List<PushStatusResponse> pushStatusRequests = claimStatus.getPushStatuses().stream()
                .map(pushStatus -> new PushStatusResponse(pushStatus.getBookingStatusId(),
                        PushStatusType.valueOf(pushStatus.getStatus()), pushStatus.getAgent(),
                        pushStatus.getCreatedTimestamp()))
                .collect(toList());
        claimStatusResponse.setPushStatuses(pushStatusRequests);

        List<BookingStatusResponse> bookingStatusResponses = claimStatus.getBookingStatuses().stream()
                .map(bookingStatus -> BookingStatusAdaptor.toResponse(bookingStatus))
                .collect(toList());
        claimStatusResponse.setBookingStatuses(bookingStatusResponses);

        return claimStatusResponse;
    }

    public ClaimStatus fromRequest(final ClaimStatusRequest claimStatusRequest) {

        BookingStatusRequest bookingStatusRequest = claimStatusRequest.getBookingStatus();
        BookingStatus bookingStatus = BookingStatusAdaptor.fromRequest(bookingStatusRequest);

        ClaimStatus claimStatus = new ClaimStatus(
                claimStatusRequest.getClaimantId(),
                false,
                Collections.singletonList(bookingStatus));
        bookingStatus.setClaimStatus(claimStatus);

        return claimStatus;
    }
}
