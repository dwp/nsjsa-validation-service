package uk.gov.dwp.jsa.validation.service.util;

import org.junit.Test;
import uk.gov.dwp.jsa.validation.service.models.http.PushStatusResponse;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PushStatusComparatorTest {

    private PushStatusComparator pushStatusComparator = new PushStatusComparator();

    @Test
    public void compareReturnsNegative() {
        PushStatusResponse pushStatusResponse1 = new PushStatusResponse();
        pushStatusResponse1.setCreatedTimestamp(LocalDateTime.now().minusDays(2));
        PushStatusResponse pushStatusResponse2 = new PushStatusResponse();
        pushStatusResponse2.setCreatedTimestamp(LocalDateTime.now());

        int compare = pushStatusComparator.compare(pushStatusResponse2, pushStatusResponse1);

        assertThat(compare < 0, is(true));
    }

    @Test
    public void compareReturnsZeroForEqualTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        PushStatusResponse pushStatusResponse1 = new PushStatusResponse();
        pushStatusResponse1.setCreatedTimestamp(now);
        PushStatusResponse pushStatusResponse2 = new PushStatusResponse();
        pushStatusResponse2.setCreatedTimestamp(now);

        int compare = pushStatusComparator.compare(pushStatusResponse1, pushStatusResponse2);

        assertThat(compare == 0, is(true));
    }
}
