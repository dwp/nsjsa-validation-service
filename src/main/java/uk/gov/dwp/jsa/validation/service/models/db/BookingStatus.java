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

@Entity(name = "booking_status")
public class BookingStatus implements Status {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID bookingStatusId;
    private String status;
    private String substatus;
    private String jobCentreCode;
    private String agent;
    @CreationTimestamp
    private LocalDateTime createdTimestamp;
    private String hash;

    @ManyToOne()
    @JoinColumn(name = "claim_status_id")
    private ClaimStatus claimStatus;

    public BookingStatus() {
    }

    public BookingStatus(
            final String status,
            final String substatus,
            final String jobCentreCode,
            final String agent,
            final String hash
    ) {
        this.status = status;
        this.substatus = substatus;
        this.jobCentreCode = jobCentreCode;
        this.agent = agent;
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

    @Override
    public String getSubstatus() {
        return substatus;
    }

    @Override
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

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
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
