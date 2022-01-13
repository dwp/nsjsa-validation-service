package uk.gov.dwp.jsa.validation.service.controllers;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ResponseFactoryTest {

    private final static Object OBJECT = new Object();
    private static final HttpStatus STATUS = HttpStatus.OK;
    private static final String PATH = "PATH";

    private ResponseFactory factory;
    private ResponseEntity<ApiResponse<Object>> responseEntity;

    @Test
    public void createsSuccessfulResponseForNonNullObject() {
        givenAFactory();
        whenICallCreateWith(OBJECT);
        thenASuccessResponseIsReturned();
    }

    @Test
    public void createsErroprResponseForNullObject() {
        givenAFactory();
        whenICallCreateWith(null);
        thenAnErrorResponseIzsCreated();
    }

    private void whenICallCreateWith(Object object) {
        responseEntity = factory.create(PATH, object);
    }

    @Test
    public void createsSuccessfulResponse() {
        givenAFactory();
        whenICallCreateSuccessfulResponse();
        thenASuccessResponseIsReturned();
    }

    @Test
    public void createErrorResponse() {
        givenAFactory();
        whenICallCreateErrorResponse();
        thenAnErrorResponseIzsCreated();
    }

    private void givenAFactory() {
        factory = new ResponseFactory();
    }

    private void whenICallCreateErrorResponse() {
        responseEntity = factory.createErrorResponse();
    }

    private void whenICallCreateSuccessfulResponse() {
        responseEntity = factory.createSuccessfulResponse(PATH, OBJECT, STATUS);
    }

    private void thenASuccessResponseIsReturned() {
        assertThat(responseEntity.getStatusCode(), is(STATUS));
        assertThat(responseEntity.getBody().getSuccess().get(0).getPath().toString(), is(PATH));
        assertThat(responseEntity.getBody().getSuccess().get(0).getData(), is(OBJECT));
    }

    private void thenAnErrorResponseIzsCreated() {
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseEntity.getBody().getError().getMessage(), is(HttpStatus.NOT_FOUND.getReasonPhrase()));
    }
}
