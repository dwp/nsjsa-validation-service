package uk.gov.dwp.jsa.validation.service.services;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.validation.service.adaptors.ClaimStatusAdaptor;
import uk.gov.dwp.jsa.validation.service.exceptions.ClaimantAlreadyExistsException;
import uk.gov.dwp.jsa.validation.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.validation.service.gateway.ClaimantServiceClient;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;
import uk.gov.dwp.jsa.validation.service.models.db.ClaimStatus;
import uk.gov.dwp.jsa.validation.service.models.db.PushStatus;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;
import uk.gov.dwp.jsa.validation.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusType;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatusRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceTest {

    private static final UUID GIVEN_CLAIMANT_ID = UUID.fromString("1e198d15-9642-4f42-9002-2e2e21eb56bf");

    private static final UUID GIVEN_BOOKING_STATUS_ID = UUID.fromString("2c554d9a-889b-46c4-b936-e22d5c52596d");

    private static final BookingStatus LATEST_BOOKING_STATUS = getSuccessStatus();
    private static final String SUCCESS_SUB_STATUS = "successSubStatus";
    private static final String SUCCESS_JOB_CENTRE_CODE = "successJobCentreCode";
    private static final String SUCCESS_AGENT = "successAgent";
    private static final String SUCCESS_HASH = "successHash";
    private static final BookingStatus PREVIOUS_BOOKING_STATUS = getNewClaimStatus();
    private static final String NEWCLAIM_SUB_STATUS = "newClaimSubstatus";
    private static final String NEWCLAIM_JOB_CENTRE_CODE = "newClaimJobCentreCode";
    private static final String NEWCLAIM_AGENT = "newClaimAgent";
    private static final String NEWCLAIM_HASH = "newclaimHash";
    private static final List<BookingStatus> BOOKING_STATUSES = Arrays.asList(
            LATEST_BOOKING_STATUS, PREVIOUS_BOOKING_STATUS
    );

    private static final BookingStatusRequest GIVEN_BOOKING_STATUS_REQUEST = getBookingStatusRequest();
    private static final ClaimStatus GIVE_CLAIM_STATUS_RECORD = new ClaimStatus(
            GIVEN_CLAIMANT_ID,
            false,
            BOOKING_STATUSES
    );

    private ValidationService validationService;

    @Mock
    private ClaimStatusRepository mockClaimStatusRepository;

    @Mock
    private ClaimStatusAdaptor mockClaimStatusAdaptor;

    @Mock
    private ClaimStatus mockClaimStatus;

    @Mock
    private ClaimStatusRequest mockClaimStatusRequest;

    @Mock
    private ClaimStatusResponse mockClaimStatusResponse;

    @Mock
    private ClaimantServiceClient mockClaimantServiceClient;

    private static final UUID ID_1 = UUID.fromString("efa61958-57b5-4c0d-b95a-8f93999c1b8e");
    private static final List<UUID> UUIDS = Arrays.asList(new UUID[] { ID_1 });

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        validationService = new ValidationService(mockClaimStatusRepository, mockClaimStatusAdaptor, mockClaimantServiceClient);
    }

    @Test
    public void getClaimStatusByClaimantId_ValidClaimantId_ClaimStatusIsReturned() {
        when(mockClaimStatus.getClaimantId()).thenReturn(ID_1);
        when(mockClaimStatus.getHash()).thenReturn("5b48bdcbe83a3833f562029a975ed2b73660b4678a883c8cd22418fa34333f76");
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenReturn(Optional.of(mockClaimStatus));
        when(mockClaimStatusAdaptor.toResponse(mockClaimStatus)).thenReturn(mockClaimStatusResponse);
        ClaimStatusResponse claimStatusByClaimantId = validationService.getClaimStatusByClaimantId(ID_1);

        assertThat(claimStatusByClaimantId, is(mockClaimStatusResponse));
    }

    @Test
    public void getClaimStatusByClaimantId_InvalidClaimantId_ClaimantByIdNotFoundExceptionIsThrown() {
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenThrow(new ClaimantByIdNotFoundException());
        expectedException.expect(ClaimantByIdNotFoundException.class);

        validationService.getClaimStatusByClaimantId(ID_1);
    }

    @Test
    public void getClaimStatusByClaimantId_HashMismatch_HttpServerErrorExceptionIsThrown() {
        when(mockClaimStatus.getClaimantId()).thenReturn(ID_1);
        when(mockClaimStatus.getHash()).thenReturn("xyz");
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenReturn(Optional.of(mockClaimStatus));

        expectedException.expect(HttpServerErrorException.class);
        expectedException.expectMessage("HashCodes Don't Match, Data Tampered");

        validationService.getClaimStatusByClaimantId(ID_1);
    }

    @Test
    public void createClaimantRequestDtoWith_ClaimStatusObject_CurrentStatusesReturned() {
        when(mockClaimStatusAdaptor.fromRequest(mockClaimStatusRequest)).thenReturn(mockClaimStatus);
        when(mockClaimStatus.getClaimantId()).thenReturn(ID_1);

        when(mockClaimStatusRepository.save(mockClaimStatus)).thenReturn(GIVE_CLAIM_STATUS_RECORD);

        ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<CurrentStatusDto> currentStatusDtoCaptor = ArgumentCaptor.forClass(CurrentStatusDto.class);

        validationService.createClaimStatus(GIVEN_CLAIMANT_ID, mockClaimStatusRequest);

        verify(mockClaimantServiceClient)
                .sendLatestStatus(uuidCaptor.capture(), currentStatusDtoCaptor.capture());

        assertThat(uuidCaptor.getValue().toString(), Is.is(GIVEN_CLAIMANT_ID.toString()));
        assertThat(currentStatusDtoCaptor.getValue().getCreatedTimestamp(), Is.is(LATEST_BOOKING_STATUS.getCreatedTimestamp()));
        assertThat(currentStatusDtoCaptor.getValue().getBookingStatus().getStatus(), Is.is(LATEST_BOOKING_STATUS.getStatus()));
        assertThat(currentStatusDtoCaptor.getValue().getBookingStatus().getSubstatus(), Is.is(LATEST_BOOKING_STATUS.getSubstatus()));
    }

    @Test
    public void createClaimStatus_ClientIdDoesNotExist_ClaimStatusIsSaved() {
        when(mockClaimStatus.getClaimantId()).thenReturn(UUID.randomUUID());
        when(mockClaimStatusAdaptor.fromRequest(mockClaimStatusRequest)).thenReturn(mockClaimStatus);
        when(mockClaimStatusRepository.save(mockClaimStatus)).thenReturn(mockClaimStatus);
        when(mockClaimStatus.getClaimStatusId()).thenReturn(ID_1);

        UUID uuid = validationService.createClaimStatus(ID_1, mockClaimStatusRequest);

        verify(mockClaimStatusRepository).save(mockClaimStatus);
        verify(mockClaimantServiceClient).sendLatestStatus(eq(ID_1), any());
        assertThat(uuid, is(ID_1));
    }

    @Test
    public void createClaimStatus_ClientIdAlreadyExists_ClaimantAlreadyExistsExceptionIsThrown() {
        when(mockClaimStatus.getClaimantId()).thenReturn(UUID.randomUUID());
        when(mockClaimStatusAdaptor.fromRequest(mockClaimStatusRequest)).thenReturn(mockClaimStatus);
        when(mockClaimStatusRepository.save(mockClaimStatus)).thenThrow(DataIntegrityViolationException.class);

        expectedException.expect(ClaimantAlreadyExistsException.class);

        validationService.createClaimStatus(ID_1, mockClaimStatusRequest);
    }

    @Test
    public void addBookingStatus_ClaimantDoesNotExists_StatusIsAdded() {
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenReturn(Optional.of(mockClaimStatus));
        when(mockClaimStatusRequest.getBookingStatus()).thenReturn(GIVEN_BOOKING_STATUS_REQUEST);

        when(mockClaimStatusRepository.save(mockClaimStatus)).thenReturn(mockClaimStatus);
        when(mockClaimStatus.getClaimStatusId()).thenReturn(ID_1);

        UUID uuid = validationService.addBookingStatus(ID_1, mockClaimStatusRequest);

        verify(mockClaimStatusRepository).save(mockClaimStatus);
        verify(mockClaimantServiceClient).sendLatestStatus(eq(ID_1), any());
        assertThat(uuid, is(ID_1));
    }

    @Test
    public void addBookingStatus_ClaimantDoesNotExists_DefaultPushStatusIsAdded() {

        final ClaimStatus requestClaimStatus = new ClaimStatus(UUID.randomUUID(), false, Collections.emptyList());
        final ClaimStatus savedClaimStatus = new ClaimStatus(UUID.randomUUID(), false, Collections.emptyList());
        savedClaimStatus.setPushStatuses(Collections.emptyList());

        when(mockClaimStatusAdaptor.fromRequest(mockClaimStatusRequest)).thenReturn(requestClaimStatus);
        when(mockClaimStatusRepository.save(requestClaimStatus)).thenReturn(savedClaimStatus);

        validationService.createClaimStatus(ID_1, mockClaimStatusRequest);

        ArgumentCaptor<ClaimStatus> captor = ArgumentCaptor.forClass(ClaimStatus.class);

        verify(mockClaimStatusRepository, times(1)).save(captor.capture());

        final List<PushStatus> pushStatuses = captor.getValue().getPushStatuses();
        final PushStatus pushStatus = pushStatuses.stream().findFirst().get();

        assertThat(pushStatus.getStatus(), is(PushStatusType.NOT_PUSHED.name()));

    }

    @Test
    public void updatePushStatus_ClaimantExists_PushStatusIsAdded() {
        final ClaimStatus savedClaimStatus = new ClaimStatus(ID_1, false, new ArrayList<>());
        savedClaimStatus.setPushStatuses(new ArrayList<>());

        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenReturn(Optional.of(savedClaimStatus));
        when(mockClaimStatusRepository.save(savedClaimStatus)).thenReturn(savedClaimStatus);

        validationService.updatePushStatus(ID_1, PushStatusType.PUSHED);

        ArgumentCaptor<ClaimStatus> captor = ArgumentCaptor.forClass(ClaimStatus.class);

        verify(mockClaimStatusRepository, times(1)).save(captor.capture());

        final List<PushStatus> pushStatuses = captor.getValue().getPushStatuses();
        final PushStatus pushStatus = pushStatuses.stream().findFirst().get();

        assertThat(pushStatus.getStatus(), is(PushStatusType.PUSHED.name()));
    }

    @Test
    public void updatePushStatus_ClaimantExists_PushStatusIsSentToClaimantService() {
        final ClaimStatus savedClaimStatus = new ClaimStatus(ID_1, false, new ArrayList<>());
        savedClaimStatus.setPushStatuses(new ArrayList<>());

        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenReturn(Optional.of(savedClaimStatus));
        when(mockClaimStatusRepository.save(savedClaimStatus)).thenReturn(savedClaimStatus);

        validationService.updatePushStatus(ID_1, PushStatusType.PUSH_FAILED);

        ArgumentCaptor<CurrentStatusDto> captor = ArgumentCaptor.forClass(CurrentStatusDto.class);

        verify(mockClaimantServiceClient, times(1)).sendLatestStatus(any(), captor.capture());

        final CurrentStatusDto.StatusDto pushStatuses = captor.getValue().getPushStatus();

        assertThat(pushStatuses.getStatus(), is(PushStatusType.PUSH_FAILED.name()));
    }

    @Test(expected = ClaimantByIdNotFoundException.class)
    public void updatePushStatus_ClaimantDoNotExists_ClaimantNotFoundIsExpected() {
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenThrow(new ClaimantByIdNotFoundException());
        validationService.updatePushStatus(ID_1, PushStatusType.PUSH_FAILED);
    }

    @Test(expected = NullPointerException.class)
    public void updatePushStatus_WithNoClaimantIdNeitherWithPushStatus_NPEIsExpected() {
        validationService.updatePushStatus(null, null);
    }

    @Test
    public void addBookingStatus_ClaimantDoesNotExists_ClaimantByIdNotFoundExceptionIsThrown() {
        expectedException.expect(ClaimantByIdNotFoundException.class);
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenThrow(new ClaimantByIdNotFoundException());

        validationService.addBookingStatus(ID_1, mockClaimStatusRequest);
    }

    @Test
    public void getStatusByClaimantIdKeepsOriginalBookingStatusListOrder() {

        final ValidationService sut = new ValidationService(mockClaimStatusRepository, new ClaimStatusAdaptor(), mockClaimantServiceClient);

        when(mockClaimStatus.getClaimantId()).thenReturn(ID_1);
        when(mockClaimStatus.getHash()).thenReturn("5b48bdcbe83a3833f562029a975ed2b73660b4678a883c8cd22418fa34333f76");
        when(mockClaimStatusRepository.findByClaimantId(ID_1)).thenReturn(Optional.of(mockClaimStatus));
        when(mockClaimStatus.getBookingStatuses()).thenReturn(BOOKING_STATUSES);

        final ClaimStatusResponse claimStatusByClaimantId = sut.getClaimStatusByClaimantId(ID_1);

        IntStream.range(0, BOOKING_STATUSES.size()).forEach(i -> {
            assertThat(claimStatusByClaimantId.getBookingStatuses().get(i).getStatus().toString(), Is.is(BOOKING_STATUSES.get(i).getStatus()));
            assertThat(claimStatusByClaimantId.getBookingStatuses().get(i).getSubstatus(), Is.is(BOOKING_STATUSES.get(i).getSubstatus()));
            assertThat(claimStatusByClaimantId.getBookingStatuses().get(i).getCreatedTimestamp(), Is.is(BOOKING_STATUSES.get(i).getCreatedTimestamp()));
            assertThat(claimStatusByClaimantId.getBookingStatuses().get(i).getAgent(), Is.is(BOOKING_STATUSES.get(i).getAgent()));
        });

    }

    @Test
    public void invalidateStatusSetsDuplicateFlag() {
        final ValidationService sut = new ValidationService(mockClaimStatusRepository, new ClaimStatusAdaptor(), mockClaimantServiceClient);

        sut.invalidateClaimStatuses(UUIDS);
        verify(mockClaimStatusRepository).invalidateStatuses(UUIDS);
    }

    private static BookingStatusRequest getBookingStatusRequest() {
        final BookingStatusRequest bookingStatusRequest = new BookingStatusRequest();
        bookingStatusRequest.setCreatedTimestamp(LocalDateTime.now());
        bookingStatusRequest.setAgent(SUCCESS_AGENT);
        bookingStatusRequest.setBookingStatusId(GIVEN_BOOKING_STATUS_ID);
        bookingStatusRequest.setJobCentreCode(SUCCESS_JOB_CENTRE_CODE);
        bookingStatusRequest.setStatus(BookingStatusType.SUCCESS);
        bookingStatusRequest.setSubstatus(SUCCESS_SUB_STATUS);
        return bookingStatusRequest;
    }

    private static BookingStatus getNewClaimStatus() {
        final BookingStatus newClaimStatus = new BookingStatus(
                BookingStatusType.NEW_CLAIM.toString(),
                NEWCLAIM_SUB_STATUS,
                NEWCLAIM_JOB_CENTRE_CODE,
                NEWCLAIM_AGENT,
                NEWCLAIM_HASH
        );
        newClaimStatus.setCreatedTimestamp(LocalDateTime.of(
                2018, Month.JANUARY, 1, 1, 0, 0
        ));
        return newClaimStatus;
    }

    private static BookingStatus getSuccessStatus() {
        final BookingStatus successLatestStatus = new BookingStatus(
                BookingStatusType.SUCCESS.toString(),
                SUCCESS_SUB_STATUS,
                SUCCESS_JOB_CENTRE_CODE,
                SUCCESS_AGENT,
                SUCCESS_HASH
        );
        successLatestStatus.setCreatedTimestamp(LocalDateTime.of(
                2018, Month.DECEMBER, 1, 1, 0, 0
        ));
        return successLatestStatus;
    }
}
