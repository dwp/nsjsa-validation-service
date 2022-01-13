package uk.gov.dwp.jsa.validation.service.util;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.dwp.jsa.validation.service.models.db.PushStatus;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusType;

import java.time.LocalDateTime;

public final class PushStatusFactory {

    private PushStatusFactory() {
    }

    public static PushStatus defaultPushStatus() {
        final String hash = DigestUtils.sha256Hex(PushStatusType.NOT_PUSHED.name());

        return new PushStatus(
                PushStatusType.NOT_PUSHED.name(),
                null,
                LocalDateTime.now(),
                hash
        );
    }

}
