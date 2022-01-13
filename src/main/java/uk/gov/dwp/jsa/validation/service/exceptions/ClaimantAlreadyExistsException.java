package uk.gov.dwp.jsa.validation.service.exceptions;

import org.springframework.http.HttpStatus;

public class ClaimantAlreadyExistsException extends RuntimeException {
    static final String CODE = HttpStatus.CONFLICT.toString();
    static final String MESSAGE = "Claimant already exists";
}
