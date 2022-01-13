package uk.gov.dwp.jsa.validation.service.models.db;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import uk.gov.dwp.jsa.validation.service.services.PushIssueSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@Entity(name = "push_issue")
public class PushIssue {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID pushIssueId;

    private String issueCode;

    @Enumerated(EnumType.STRING)
    private PushIssueSource source;

    private String fullReason;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    private String hash;

    @ManyToOne()
    @JoinColumn(name = "claim_status_id")
    private ClaimStatus claimStatus;

    public PushIssue() {
    }

    public PushIssue(final String issueCode,
                     final String fullReason,
                     final PushIssueSource source,
                     final ClaimStatus claimStatus) {
        this.issueCode = issueCode;
        this.source = source;
        this.fullReason = fullReason;
        this.claimStatus = claimStatus;
    }

    @PrePersist
    public void prePersist() {
        hash = DigestUtils.sha256Hex(
                claimStatus.getClaimantId().toString()
        );
    }

    public UUID getPushIssueId() {
        return pushIssueId;
    }

    public void setPushIssueId(final UUID pushIssueId) {
        this.pushIssueId = pushIssueId;
    }

    public String getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(final String issueCode) {
        this.issueCode = issueCode;
    }

    public PushIssueSource getSource() {
        return source;
    }

    public void setSource(final PushIssueSource source) {
        this.source = source;
    }

    public String getFullReason() {
        return fullReason;
    }

    public void setFullReason(final String fullReason) {
        this.fullReason = fullReason;
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

    @Override
    public boolean equals(final Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

}
