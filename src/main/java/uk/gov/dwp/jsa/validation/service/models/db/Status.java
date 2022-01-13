package uk.gov.dwp.jsa.validation.service.models.db;

import java.time.LocalDateTime;

public interface Status {

    void setStatus(final String status);

    String getStatus();

    /* for some status, substatus is optional, therefore a default set implementation */
    default void setSubstatus(final String substatus) {
    }

    /* for some status, substatus is optional, therefore a default get implementation */
    default String getSubstatus() {
        return null;
    }

    void setCreatedTimestamp(final LocalDateTime createdTimestamp);

    LocalDateTime getCreatedTimestamp();

}
