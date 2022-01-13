package uk.gov.dwp.jsa.validation.service.models.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@Entity(name = "push_status")
public class PushStatus implements Status {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID bookingStatusId;
    private String status;
    private String agent;
    @CreationTimestamp
    private LocalDateTime createdTimestamp;
    private String hash;

    @ManyToOne()
    @JoinColumn(name = "claim_status_id")
    private ClaimStatus claimStatus;

    public PushStatus() {
    }

    public PushStatus(final String status, final String agent, final LocalDateTime createdTimestamp,
                      final String hash, final ClaimStatus claimStatus) {
        this.status = status;
        this.agent = agent;
        this.createdTimestamp = createdTimestamp;
        this.hash = hash;
        this.claimStatus = claimStatus;
    }

    public PushStatus(final String status, final String agent,
                      final LocalDateTime createdTimestamp, final String hash) {

        this.status = status;
        this.agent = agent;
        this.createdTimestamp = createdTimestamp;
        this.hash = hash;
    }

    public UUID getBookingStatusId() {
        return bookingStatusId;
    }

    public void setBookingStatusId(final UUID bookingStatusId) {
        this.bookingStatusId = bookingStatusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(final String agent) {
        this.agent = agent;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(final ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
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
