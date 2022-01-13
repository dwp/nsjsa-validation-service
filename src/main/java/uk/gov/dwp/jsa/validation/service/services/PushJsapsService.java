package uk.gov.dwp.jsa.validation.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.adaptors.JsapsServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.dto.jsaps.JsapsRequest;
import uk.gov.dwp.jsa.adaptors.exception.PushJsapsException;

import uk.gov.dwp.jsa.validation.service.models.http.PushStatusType;

import java.util.Optional;
import java.util.concurrent.ExecutionException;


@Service
public class PushJsapsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushJsapsService.class);

    private final JsapsServiceAdaptor jsapsServiceAdaptor;
    private ValidationService validationService;
    private PushIssueService pushIssueService;

    @Autowired
    public PushJsapsService(final JsapsServiceAdaptor jsapsServiceAdaptor, final ValidationService validationService,
                            final PushIssueService pushIssueService) {
        this.jsapsServiceAdaptor = jsapsServiceAdaptor;
        this.validationService = validationService;
        this.pushIssueService = pushIssueService;
    }

    @Async
    public void pushToJsaps(final JsapsRequest request) {
        LOGGER.debug("Pushing to Jsaps : ClaimantId: {}", request.getClaimant().getClaimantId());
        try {
            Optional<String> messages = jsapsServiceAdaptor.pushToJsaps(request.getClaimant().getClaimantId(),
                    request).get();
            validationService.updatePushStatus(request.getClaimant().getClaimantId(), PushStatusType.PUSHED);
            messages.ifPresent(s -> pushIssueService.saveJsapsPushIssues(request.getClaimant().getClaimantId(), s));
        } catch (PushJsapsException e) {
            StringBuilder builder = new StringBuilder();
            e.getApiErrors()
                    .forEach(er -> builder.append("Code:" + er.getCode() + " Message:" + er.getMessage() + "--"));
            LOGGER.error("PushJsapsException: Pushing to Jsaps : ClaimantId: {} with Messages {}, {}",
                    request.getClaimant().getClaimantId(), builder.toString(), e.getMessage());
            pushIssueService.saveJsapsPushIssues(request.getClaimant().getClaimantId(), builder.toString());
            validationService.updatePushStatus(request.getClaimant().getClaimantId(), PushStatusType.PUSH_FAILED);
        } catch (ExecutionException e) {
            LOGGER.error("JSAPS Request failed to complete for claimant {}, with message {}",
                    request.getClaimant().getClaimantId(), e.getMessage());
            validationService.updatePushStatus(request.getClaimant().getClaimantId(), PushStatusType.PUSH_FAILED);
            pushIssueService.saveJsapsPushIssues(request.getClaimant().getClaimantId(),
                    "E0001: Execution Exception getting messages from Push. " + e.getMessage());
        } catch (InterruptedException ie) {
            LOGGER.error("Failed to get messages for jsaps push for claimant {}, with message {}",
                    request.getClaimant().getClaimantId(), ie.getMessage());
            validationService.updatePushStatus(request.getClaimant().getClaimantId(), PushStatusType.PUSH_FAILED);
            pushIssueService.saveJsapsPushIssues(request.getClaimant().getClaimantId(),
                    "E0002: Interrupted Exception getting messages from Push. " + ie.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
