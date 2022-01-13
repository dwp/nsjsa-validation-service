package uk.gov.dwp.jsa.validation.service.models.http;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class PushStatusResponse {

    private UUID id;
    private PushStatusType status;
    private String agent;
    private LocalDateTime createdTimestamp;

    public PushStatusResponse() {
    }

    public PushStatusResponse(final UUID id, final PushStatusType status,
                              final String agent, final LocalDateTime createdTimestamp) {
        this.id = id;
        this.status = status;
        this.agent = agent;
        this.createdTimestamp = createdTimestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
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
