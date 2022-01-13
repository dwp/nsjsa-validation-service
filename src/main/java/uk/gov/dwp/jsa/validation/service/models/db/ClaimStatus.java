package uk.gov.dwp.jsa.validation.service.models.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@Entity(name = "claim_status")
public class ClaimStatus {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID claimStatusId;
    private UUID claimantId;
    private boolean locked;
    private boolean isDuplicate;
    @CreationTimestamp
    private LocalDateTime createdTimestamp;
    @UpdateTimestamp
    private LocalDateTime updatedTimestamp;
    private String hash;

    @OrderBy("createdTimestamp DESC")
    @OneToMany(mappedBy = "claimStatus", cascade = CascadeType.ALL)
    private List<BookingStatus> bookingStatuses = new ArrayList<>();

    @OrderBy("createdTimestamp DESC")
    @OneToMany(mappedBy = "claimStatus", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PushStatus> pushStatuses = new ArrayList<>();

    public ClaimStatus() {
    }

    public ClaimStatus(final UUID claimantId, final boolean locked,
                       final List<BookingStatus> bookingStatuses) {
        this.claimantId = claimantId;
        this.locked = locked;
        this.bookingStatuses = bookingStatuses;
        this.isDuplicate = false;
    }

    public UUID getClaimStatusId() {
        return claimStatusId;
    }

    public void setClaimStatusId(final UUID claimStatusId) {
        this.claimStatusId = claimStatusId;
    }

    public UUID getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(final UUID claimantId) {
        this.claimantId = claimantId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(final boolean duplicate) {
        isDuplicate = duplicate;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public List<BookingStatus> getBookingStatuses() {
        return bookingStatuses;
    }

    public void setBookingStatuses(final List<BookingStatus> bookingStatuses) {
        this.bookingStatuses = bookingStatuses;
    }

    public List<PushStatus> getPushStatuses() {
        return pushStatuses;
    }

    public void setPushStatuses(final List<PushStatus> pushStatuses) {
        this.pushStatuses = pushStatuses;
    }

    public void setCreatedTimestamp(final LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public void setUpdatedTimestamp(final LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(final Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this, "hash");
    }
}
