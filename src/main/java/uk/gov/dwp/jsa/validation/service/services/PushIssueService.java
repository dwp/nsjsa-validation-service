package uk.gov.dwp.jsa.validation.service.services;

import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.validation.service.models.db.ClaimStatus;
import uk.gov.dwp.jsa.validation.service.models.db.PushIssue;
import uk.gov.dwp.jsa.validation.service.repositories.ClaimStatusRepository;
import uk.gov.dwp.jsa.validation.service.repositories.PushIssuesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PushIssueService {

    private static final String DELIMITER = "--";
    private static final int CODE_START = 0;
    private static final int CODE_END = 5;
    private static final int MESSAGE_START = 6;

    private ClaimStatusRepository claimStatusRepository;
    private PushIssuesRepository pushIssuesRepository;

    public PushIssueService(final ClaimStatusRepository claimStatusRepository,
                            final PushIssuesRepository pushIssuesRepository) {
        this.claimStatusRepository = claimStatusRepository;
        this.pushIssuesRepository = pushIssuesRepository;
    }


    public List<PushIssue> saveJsapsPushIssues(final UUID claimantId, final String messages) {
        final Optional<ClaimStatus> claimStatusOptional = claimStatusRepository.findByClaimantId(claimantId);
        if (claimStatusOptional.isPresent()) {
            List<PushIssue> pushIssues = processMessages(messages, claimStatusOptional.get());
            return (List<PushIssue>) pushIssuesRepository.saveAll(pushIssues);
        }
        return null;
    }

    private List<PushIssue> processMessages(final String messages, final ClaimStatus claimStatus) {
        List<PushIssue> pushIssues = new ArrayList<>();
        String[] splitMessages = messages.split(DELIMITER);
        for (String s : splitMessages) {
            String code = s.substring(CODE_START, CODE_END);
            String message = s.substring(MESSAGE_START).trim();
            PushIssue pi = new PushIssue(code, message, PushIssueSource.JSAPS, claimStatus);
            pushIssues.add(pi);
        }
        return pushIssues;
    }


}
