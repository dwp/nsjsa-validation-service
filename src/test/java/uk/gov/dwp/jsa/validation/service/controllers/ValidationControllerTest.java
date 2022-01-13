package uk.gov.dwp.jsa.validation.service.controllers;

import org.jooq.tools.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.adaptors.dto.claim.validation.PushClaimResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.validation.service.AppInfo;
import uk.gov.dwp.jsa.validation.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.validation.service.models.http.BookingStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusRequest;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;
import uk.gov.dwp.jsa.validation.service.services.PushClaimService;
import uk.gov.dwp.jsa.validation.service.services.ValidationService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class ValidationControllerTest {

    private static final String CLAIMANT_ID = "14d110fb-385a-417e-b07e-48a1bf89add0";
    private static final UUID CLAIMANT_UUID = UUID.fromString(CLAIMANT_ID);
    private static final String CLAIM_STATUS_ID = "14d110fb-385a-417e-b07e-48a1bf89add1";
    private static final UUID CLAIM_STATUS_UUID = UUID.fromString(CLAIM_STATUS_ID);
    private static final String GET_URL = "http://localhost/claim/v1/" + CLAIMANT_ID + "/validation";
    private static final List<UUID> CLAIMANT_UUIDS = Arrays.asList(new UUID[] { CLAIMANT_UUID });

    private ValidationController controller;

    @Mock
    private ValidationService validationService;

    @Mock
    private PushClaimService pushClaimService;

    @Mock
    private HttpServletRequest httpServletRequest;

    private ResponseFactory responseFactory = new ResponseFactory();

    @Mock
    private AppInfo appInfo;

    private ClaimStatusResponse claimStatusResponse = new ClaimStatusResponse();
    private ClaimStatusRequest claimStatusRequest = new ClaimStatusRequest();
    private PushClaimResponse citizenUpdateResponses = new PushClaimResponse();


    @Before
    public void before() throws ExecutionException, InterruptedException {
        when(appInfo.getVersion()).thenReturn(StringUtils.EMPTY);

        controller = new ValidationController(
                validationService,
                pushClaimService,
                new WithVersionUriComponentsBuilder(appInfo),
                responseFactory
        );

        given(httpServletRequest.getRequestURI()).willReturn(GET_URL);
        given(validationService.getClaimStatusByClaimantId(CLAIMANT_UUID)).willReturn(claimStatusResponse);
        given(validationService.createClaimStatus(any(), any())).willReturn(CLAIM_STATUS_UUID);
        given(validationService.addBookingStatus(any(), any())).willReturn(CLAIM_STATUS_UUID);
        given(pushClaimService.pushClaim(any())).willReturn(citizenUpdateResponses);
        given(pushClaimService.pushClaimDrs(CLAIMANT_UUID)).willReturn(true);

        BookingStatusRequest bookingStatusRequest = new BookingStatusRequest();
        bookingStatusRequest.setStatus(BookingStatusType.SUCCESS);
        claimStatusRequest.setClaimantId(CLAIMANT_UUID);
        claimStatusRequest.setBookingStatus(bookingStatusRequest);
    }

    @Test
    public void getClaimStatus_validClaimantId_httpStatusIs200() {

        ResponseEntity<ApiResponse<ClaimStatusResponse>> response =
                controller.getClaimStatus(CLAIMANT_UUID, httpServletRequest);

        assertThat(response.getStatusCodeValue(), is(equalTo(200)));
    }

    @Test
    public void getClaimStatus_validClaimantId_correctPathIsReturned() {

        ResponseEntity<ApiResponse<ClaimStatusResponse>> response =
                controller.getClaimStatus(CLAIMANT_UUID, httpServletRequest);

        assertThat(response.getBody().getSuccess(), hasSize(1));
        assertThat(response.getBody().getSuccess().get(0).getPath().toString(), is(equalTo(GET_URL)));
    }

    @Test
    public void getClaimStatus_validClaimantId_correctClaimStatusDataIsReturned() {

        ResponseEntity<ApiResponse<ClaimStatusResponse>> response =
                controller.getClaimStatus(CLAIMANT_UUID, httpServletRequest);

        assertThat(response.getBody().getSuccess().get(0).getData(), is(sameInstance(claimStatusResponse)));
    }

    @Test
    public void getClaimStatus_thereIsAnError_httpStatusIs404() {
        given(validationService.getClaimStatusByClaimantId(CLAIMANT_UUID)).willReturn(null);

        ResponseEntity<ApiResponse<ClaimStatusResponse>> response =
                controller.getClaimStatus(CLAIMANT_UUID, httpServletRequest);

        assertThat(response.getStatusCodeValue(), is(equalTo(404)));
    }

    @Test
    public void getClaimStatus_thereIsAnError_ApiMessageIsNotFound() {
        given(validationService.getClaimStatusByClaimantId(CLAIMANT_UUID)).willReturn(null);

        ResponseEntity<ApiResponse<ClaimStatusResponse>> response =
                controller.getClaimStatus(CLAIMANT_UUID, httpServletRequest);

        assertThat(response.getBody().getError().getMessage(), is(equalTo("Not Found")));
    }

    @Test
    public void createClaimStatus_validClaimStatus_claimStatusIsStored() {
        controller.createClaimStatus(CLAIMANT_UUID, claimStatusRequest);
        verify(validationService).createClaimStatus(CLAIMANT_UUID, claimStatusRequest);
    }

    @Test
    public void createClaimStatus_validClaimStatus_UuidIsReturned() {
        ResponseEntity<ApiResponse<UUID>> response =
                controller.createClaimStatus(CLAIMANT_UUID, claimStatusRequest);

        assertThat(response.getBody().getSuccess(), hasSize(1));
        assertThat(response.getBody().getSuccess().get(0).getData(), is(equalTo(CLAIM_STATUS_UUID)));
    }

    @Test
    public void addBookingStatus_validClaimStatus_claimStatusIsStored() {
        controller.addBookingStatus(CLAIMANT_UUID, claimStatusRequest);
        verify(validationService).addBookingStatus(CLAIMANT_UUID, claimStatusRequest);
    }

    @Test
    public void checkDRSPushIsDoneOnSuccess() throws ExecutionException, InterruptedException {
        controller.addBookingStatus(CLAIMANT_UUID, claimStatusRequest);
        verify(pushClaimService, times(1)).pushClaimDrs(CLAIMANT_UUID);
    }

    @Test
    public void checkDRSPushFailureIsHandled() throws ExecutionException, InterruptedException {
        UUID failClaimID = UUID.randomUUID();

        claimStatusRequest.setClaimantId(failClaimID);
        controller.addBookingStatus(failClaimID, claimStatusRequest);
        verify(pushClaimService, times(1)).pushClaimDrs(failClaimID);
    }

    @Test
    public void checkDRSPushIsNotDoneOnNotSuccess() throws ExecutionException, InterruptedException {
        claimStatusRequest.getBookingStatus().setStatus(BookingStatusType.FINAL_FAIL);
        controller.addBookingStatus(CLAIMANT_UUID, claimStatusRequest);
        verify(pushClaimService, times(0)).pushClaimDrs(CLAIMANT_UUID);
    }

    @Test
    public void addBookingStatus_validClaimStatus_UuidIsReturned() {
        ResponseEntity<ApiResponse<UUID>> response =
                controller.addBookingStatus(CLAIMANT_UUID, claimStatusRequest);

        assertThat(response.getBody().getSuccess(), hasSize(1));
        assertThat(response.getBody().getSuccess().get(0).getData(), is(equalTo(CLAIM_STATUS_UUID)));
    }

    @Test
    public void pushClaim_Returns_PushStatusResponse() throws ExecutionException, InterruptedException {
        ResponseEntity<ApiResponse<PushClaimResponse>> response =
                controller.pushClaim(CLAIMANT_UUID);

        assertThat(response.getBody().getSuccess(), hasSize(1));
        assertThat(response.getBody().getSuccess().get(0).getData(), is(citizenUpdateResponses));
        assertThat(response.getStatusCode(), is(HttpStatus.ACCEPTED));
    }

    @Test
    public void invalidateClaim_Returns_InvalidateStatusResponse() {
        ResponseEntity<ApiResponse<Boolean>> response =
                controller.invalidateClaimStatus(CLAIMANT_UUIDS);

        assertThat(response.getBody().getSuccess(), hasSize(1));
        assertThat(response.getBody().getSuccess().get(0).getData(), is(true));
        assertThat(response.getStatusCode(), is(HttpStatus.ACCEPTED));
    }
}
