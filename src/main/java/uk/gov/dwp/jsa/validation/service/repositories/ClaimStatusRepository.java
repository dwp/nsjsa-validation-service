package uk.gov.dwp.jsa.validation.service.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dwp.jsa.validation.service.models.db.ClaimStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimStatusRepository extends CrudRepository<ClaimStatus, UUID> {

    Optional<ClaimStatus> findByClaimantId(final UUID claimantId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE claim_status as cs SET cs.isDuplicate = true WHERE cs.claimantId IN (:claimantIdList)")
    void invalidateStatuses(@Param("claimantIdList") List<UUID> claimantIdList);
}
