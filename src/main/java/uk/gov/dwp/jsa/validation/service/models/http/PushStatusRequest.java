package uk.gov.dwp.jsa.validation.service.models.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PushStatusRequest {

    private UUID bookingStatusId;
    private PushStatusType status;
    private String agent;
    private LocalDateTime createdTimestamp;

    public PushStatusRequest() {
    }

    public PushStatusRequest(final UUID bookingStatusId, final PushStatusType status,
                             final String agent, final LocalDateTime createdTimestamp) {
        this.bookingStatusId = bookingStatusId;
        this.status = status;
        this.agent = agent;
        this.createdTimestamp = createdTimestamp;
    }

    public UUID getBookingStatusId() {
        return bookingStatusId;
    }

    public void setBookingStatusId(final UUID bookingStatusId) {
        this.bookingStatusId = bookingStatusId;
    }

    public PushStatusType getStatus() {
        return status;
    }

    public void setStatus(final PushStatusType status) {
        this.status = status;
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
