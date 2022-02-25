package uk.gov.dwp.jsa.validation.service.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.jsa.validation.service.models.db.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;


@Repository
public interface BookingStatusRepository extends CrudRepository<BookingStatus, UUID> {

    /**
     * Gets the number of assisted digital claims for a given range.
     *
     * This query works by searching for booking status of NEW_CLAIM and where the agent is not null. When an agent
     * fills out a claim on behalf of a citizen their ID is stored in the agent field. It has been agreed that this
     * count will include duplicate claims. The count is performed by doing a distinct count on the claim status ID
     * in case there are multiple booking status records returned for a single claim.
     *
     * @param startDateTime start range (inclusive)
     * @param endDateTime end range (inclusive)
     *
     * @return the number of assisted digital claims for a given range
     */
    @Query(value =
            "SELECT COUNT(DISTINCT bs.claim_status_id) "
                    + "FROM {h-schema}booking_status bs "
                    + "WHERE bs.created_timestamp >= :startDateTime AND bs.created_timestamp <= :endDateTime "
                    + "AND bs.status = 'NEW_CLAIM' AND bs.agent IS NOT NULL",
            nativeQuery = true)
    int getAssistedDigitalClaimCount(@Param("startDateTime") final LocalDateTime startDateTime,
                                     @Param("endDateTime") final LocalDateTime endDateTime);
}
