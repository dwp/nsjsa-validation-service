package uk.gov.dwp.jsa.validation.service.util;

import uk.gov.dwp.jsa.validation.service.models.http.PushStatusResponse;

import java.io.Serializable;
import java.util.Comparator;

public class PushStatusComparator implements Comparator<PushStatusResponse>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final PushStatusResponse pushStatus1, final PushStatusResponse pushStatus2) {
        return pushStatus2.getCreatedTimestamp().compareTo(pushStatus1.getCreatedTimestamp());
    }
}
