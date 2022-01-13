package uk.gov.dwp.jsa.validation.service.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.dwp.jsa.adaptors.JsapsServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.dto.jsaps.JsapsRequest;
import uk.gov.dwp.jsa.adaptors.exception.PushJsapsException;
import uk.gov.dwp.jsa.adaptors.http.api.ApiError;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

public class PushJsapsServiceTest {

    private static final UUID CLAIMANT_ID = UUID.randomUUID();

    private JsapsRequest request;

    private JsapsServiceAdaptor jsapsServiceAdaptor;

    private ValidationService validationService;

    private PushIssueService pushIssueService;

    private PushJsapsService classToBeTested;


    @Before
    public void setup() {
        request = Mockito.mock(JsapsRequest.class, Mockito.RETURNS_DEEP_STUBS);
        jsapsServiceAdaptor = Mockito.mock(JsapsServiceAdaptor.class);
        validationService = Mockito.mock(ValidationService.class);
        pushIssueService = Mockito.mock(PushIssueService.class);
        classToBeTested = new PushJsapsService(jsapsServiceAdaptor, validationService, pushIssueService);
    }

    @Test
    public void givenPush_WhenPushed_ThenAdaptorIsCalled() {
        givenPushObject();
        whenPushed();
        thenPushStatusPushed();
    }



    @Test
    public void givenBadPushObject_WhenPushed_ThenPushStatusFailed() {
        givenBadPushObject();
        whenPushed();
        thenPushStatusFailed();
    }

    @Test
    public void givenPushInterrupted_ThenPushStatusFailed() throws ExecutionException, InterruptedException {
        givenInterruptedPushObject();
        whenPushed();
        thenPushStatusFailed();
    }

    @Test
    public void givenExecutionException_ThenPushStatusFailed() throws ExecutionException, InterruptedException {
        givenExecutionError();
        whenPushed();
        thenPushStatusFailed();
    }

    private void givenPushObject() {
        when(jsapsServiceAdaptor.pushToJsaps(any(), any())).thenReturn(CompletableFuture.completedFuture(
                Optional.of("W1111: Warning Message")));
        when(request.getClaimant().getClaimantId()).thenReturn(CLAIMANT_ID);

    }


    private void givenBadPushObject() {
        when(request.getClaimant().getClaimantId()).thenReturn(CLAIMANT_ID);
        List<ApiError> list = Arrays.asList(new ApiError("code","message"),new ApiError("code1","message1"));
        PushJsapsException exception = new PushJsapsException(list);
        when(jsapsServiceAdaptor.pushToJsaps(CLAIMANT_ID,request)).thenThrow(exception);

    }

    private void givenInterruptedPushObject() throws ExecutionException, InterruptedException {
        when(request.getClaimant().getClaimantId()).thenReturn(CLAIMANT_ID);
        CompletableFuture<Optional<String>> optionalCompletableFuture = mock(CompletableFuture.class);
        when(jsapsServiceAdaptor.pushToJsaps(CLAIMANT_ID,request)).thenReturn(optionalCompletableFuture);
        when(optionalCompletableFuture.get()).thenThrow(InterruptedException.class);

    }

    private void givenExecutionError() throws ExecutionException, InterruptedException {
        when(request.getClaimant().getClaimantId()).thenReturn(CLAIMANT_ID);
        CompletableFuture<Optional<String>> optionalCompletableFuture = mock(CompletableFuture.class);
        when(jsapsServiceAdaptor.pushToJsaps(CLAIMANT_ID,request)).thenReturn(optionalCompletableFuture);
        when(optionalCompletableFuture.get()).thenThrow(ExecutionException.class);

    }

    private void whenPushed() {
        classToBeTested.pushToJsaps(request);
    }

    private void thenPushStatusPushed() {
        verify(jsapsServiceAdaptor, times(1)).pushToJsaps(CLAIMANT_ID, request);
        verify(validationService, times(1)).updatePushStatus(CLAIMANT_ID, PushStatusType.PUSHED);
        verify(pushIssueService, times(1)).saveJsapsPushIssues(any(), any());
    }

    private void thenPushStatusFailed() {
        verify(jsapsServiceAdaptor, times(1)).pushToJsaps(CLAIMANT_ID, request);
        verify(validationService, times(1)).updatePushStatus(CLAIMANT_ID, PushStatusType.PUSH_FAILED);
    }
}
