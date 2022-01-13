package uk.gov.dwp.jsa.validation.service.exceptions;

import org.springframework.http.HttpStatus;

public class ClaimantNotFoundException extends RuntimeException {
    static final String CODE = HttpStatus.NOT_FOUND.toString();
    static final String MESSAGE = "Could not find a claimant by id or by nino in CIS";
}
