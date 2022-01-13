package uk.gov.dwp.jsa.validation.service.repositories;

import org.springframework.data.repository.CrudRepository;
import uk.gov.dwp.jsa.validation.service.models.db.PushIssue;

import java.util.UUID;

public interface PushIssuesRepository extends CrudRepository<PushIssue, UUID> {

}
