package uk.gov.dwp.jsa.validation.service.models.http;

import org.apache.commons.lang3.StringUtils;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.validation.service.util.BookingStatusComparator;
import uk.gov.dwp.jsa.validation.service.util.PushStatusComparator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class ClaimStatusResponse {

    private UUID claimantId;
    private boolean locked;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    private List<BookingStatusResponse> bookingStatuses = new ArrayList<>();

    private List<PushStatusResponse> pushStatuses = new ArrayList<>();

    public ClaimStatusResponse() {
    }

    public ClaimStatusResponse(
            final UUID claimantId,
            final boolean locked
    ) {
        this.claimantId = claimantId;
        this.locked = locked;
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

    public List<BookingStatusResponse> getBookingStatuses() {
        return bookingStatuses;
    }

    public void setBookingStatuses(final List<BookingStatusResponse> bookingStatuses) {
        this.bookingStatuses = bookingStatuses;
    }

    public List<PushStatusResponse> getPushStatuses() {
        return pushStatuses;
    }

    public void setPushStatuses(final List<PushStatusResponse> pushStatuses) {
        this.pushStatuses = pushStatuses;
    }

    public BookingStatusResponse latestBookingStatus() {
        Collections.sort(bookingStatuses, new BookingStatusComparator());
        assert (!bookingStatuses.isEmpty());
        return bookingStatuses.get(0);
    }

    public Optional<String> getJobCentreCode() {
        Collections.sort(bookingStatuses, new BookingStatusComparator());
        assert (!bookingStatuses.isEmpty());
        final Optional<BookingStatusResponse> bookingStatusResponseOptional =
                bookingStatuses.stream().filter(b -> StringUtils.isNotEmpty(b.getJobCentreCode())).findFirst();
        if (bookingStatusResponseOptional.isPresent()) {
            return Optional.of(bookingStatusResponseOptional.get().getJobCentreCode());
        }
        return Optional.empty();
    }

    public PushStatusResponse latestPushStatus() {
        Collections.sort(pushStatuses, new PushStatusComparator());
        assert (!pushStatuses.isEmpty());
        return pushStatuses.get(0);
    }


    @Override
    public boolean equals(final Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    public boolean isClaimCreatedByAgent() {
        final Optional<BookingStatusResponse> newClaimStatus =
                bookingStatuses
                        .stream()
                        .filter(e -> e.getStatus() == BookingStatusType.NEW_CLAIM)
                        .findFirst();
        if (newClaimStatus.isPresent()) {
            return StringUtils.isNotBlank(newClaimStatus.get().getAgent());
        }
        return false;
    }
}
