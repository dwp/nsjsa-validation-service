package uk.gov.dwp.jsa.validation.service.models.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingStatusRequest {

    private UUID bookingStatusId;
    private BookingStatusType status;
    private String substatus;
    private String jobCentreCode;
    private String agent;
    private LocalDateTime createdTimestamp;

    public BookingStatusRequest() {
    }

    public BookingStatusRequest(final UUID bookingStatusId, final BookingStatusType status,
                                final String substatus, final String jobCentreCode,
                                final String agent, final LocalDateTime createdTimestamp) {
        this.bookingStatusId = bookingStatusId;
        this.status = status;
        this.substatus = substatus;
        this.jobCentreCode = jobCentreCode;
        this.agent = agent;
        this.createdTimestamp = createdTimestamp;
    }

    public UUID getBookingStatusId() {
        return bookingStatusId;
    }

    public void setBookingStatusId(final UUID bookingStatusId) {
        this.bookingStatusId = bookingStatusId;
    }

    public BookingStatusType getStatus() {
        return status;
    }

    public void setStatus(final BookingStatusType status) {
        this.status = status;
    }

    public String getSubstatus() {
        return substatus;
    }

    public void setSubstatus(final String substatus) {
        this.substatus = substatus;
    }

    public String getJobCentreCode() {
        return jobCentreCode;
    }

    public void setJobCentreCode(final String jobCentreCode) {
        this.jobCentreCode = jobCentreCode;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(final String agent) {
        this.agent = agent;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(final LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @Override
    public boolean equals(final Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }
}
