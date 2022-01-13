package uk.gov.dwp.jsa.validation.service.adaptors;

import org.junit.Test;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;
import uk.gov.dwp.jsa.validation.service.models.db.ClaimStatus;
import uk.gov.dwp.jsa.validation.service.models.db.PushStatus;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.dwp.jsa.validation.service.models.http.PushStatusType.CLERICAL;

public class ClaimStatusAdaptorTest {

    private ClaimStatusAdaptor claimStatusAdaptor = new ClaimStatusAdaptor();

    private  static final String JOB_CODE = "JOB_CODE";

    private  static final String AGENT = "AGENT";

    private  static final String APPOINTEE_LETTER = "Appointee Letter";

    private static final UUID ID_1 = UUID.randomUUID();

    private static final UUID ID_2 = UUID.randomUUID();

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void claimStatusIsAdaptedToClaimStatusResponseSuccessfully() {
        ClaimStatus claimStatus = givenClaimStatusIsSet();

        ClaimStatusResponse claimStatusResponse = claimStatusAdaptor.toResponse(claimStatus);

        thenClaimStatusIsAdaptedAsExpected(claimStatusResponse);
    }


    @Test
    public void claimStatusRequestIsAdaptedToClaimStatusSuccessfully() {
        ClaimStatusRequest claimStatusRequest = givenClaimStatusRequestIsSet();

        ClaimStatus claimStatus = claimStatusAdaptor.fromRequest(claimStatusRequest);

        assertThat(claimStatus.getClaimStatusId(), nullValue());
        assertThat(claimStatus.getClaimantId(), is(ID_1));
        assertThat(claimStatus.isLocked(), is(false));
        assertThat(claimStatus.isDuplicate(), is(false));
        assertThat(claimStatus.getBookingStatuses().get(0).getStatus(), is(BookingStatusType.NEW_CLAIM.name()));
        assertThat(claimStatus.getBookingStatuses().get(0).getSubstatus(), is(APPOINTEE_LETTER));
        assertThat(claimStatus.getBookingStatuses().get(0).getJobCentreCode(), is(JOB_CODE));
    }

    private ClaimStatusRequest givenClaimStatusRequestIsSet() {
        ClaimStatusRequest claimStatusRequest = new ClaimStatusRequest();
        claimStatusRequest.setClaimantId(ID_1);
        claimStatusRequest.setClaimStatusId(ID_2);
        claimStatusRequest.setBookingStatus(new BookingStatusRequest(ID_1, BookingStatusType.NEW_CLAIM,
                APPOINTEE_LETTER, JOB_CODE, AGENT, NOW));
        return claimStatusRequest;
    }


    private void thenClaimStatusIsAdaptedAsExpected(final ClaimStatusResponse claimStatusResponse) {
        assertThat(claimStatusResponse.getClaimantId(), is(ID_1));
        assertThat(claimStatusResponse.getCreatedTimestamp(), is(NOW));
        assertThat(claimStatusResponse.getUpdatedTimestamp(), is(NOW));
        assertThat(claimStatusResponse.isLocked(), is(true));
        assertThat(claimStatusResponse.getBookingStatuses().get(0).getStatus(), is(BookingStatusType.NEW_CLAIM));
        assertThat(claimStatusResponse.getBookingStatuses().get(0).getSubstatus(), is(APPOINTEE_LETTER));
        assertThat(claimStatusResponse.getBookingStatuses().get(0).getJobCentreCode(), is(JOB_CODE));
        assertThat(claimStatusResponse.getBookingStatuses().get(0).getAgent(), is(AGENT));
        assertThat(claimStatusResponse.getPushStatuses().get(0).getStatus(), is(CLERICAL));
        assertThat(claimStatusResponse.getPushStatuses().get(0).getAgent(), is(AGENT));
    }

    private ClaimStatus givenClaimStatusIsSet() {
        ClaimStatus claimStatus = new ClaimStatus();
        claimStatus.setClaimantId(ID_1);
        claimStatus.setClaimStatusId(ID_2);
        claimStatus.setHash("12345667890");
        claimStatus.setCreatedTimestamp(NOW);
        claimStatus.setLocked(true);
        claimStatus.setDuplicate(true);
        claimStatus.setUpdatedTimestamp(NOW);

        List<BookingStatus> bookingStatuses = new ArrayList<>();
        String hash = "234455T";
        bookingStatuses.add(new BookingStatus(BookingStatusType.NEW_CLAIM.name(), APPOINTEE_LETTER,
                JOB_CODE, AGENT, hash));
        claimStatus.setBookingStatuses(bookingStatuses);

        List<PushStatus> pushStatuses = new ArrayList<>();
        pushStatuses.add(new PushStatus(CLERICAL.name(), AGENT, NOW, hash, claimStatus));
        claimStatus.setPushStatuses(pushStatuses);
        return claimStatus;
    }

}
