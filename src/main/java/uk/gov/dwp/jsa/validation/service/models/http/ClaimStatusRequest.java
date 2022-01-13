package uk.gov.dwp.jsa.validation.service.models.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimStatusRequest {

    private UUID claimStatusId;
    private UUID claimantId;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    private BookingStatusRequest bookingStatus;

    public ClaimStatusRequest() {
    }

    public ClaimStatusRequest(
            final UUID claimantId
    ) {
        this.claimantId = claimantId;
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

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(final LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(final LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public BookingStatusRequest getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(final BookingStatusRequest bookingStatus) {
        this.bookingStatus = bookingStatus;
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
