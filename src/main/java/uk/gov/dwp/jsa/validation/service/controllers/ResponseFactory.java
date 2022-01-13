package uk.gov.dwp.jsa.validation.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.validation.service.services.ResponseBuilder;

import java.net.URI;

@Component
public class ResponseFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFactory.class);

    public <T> ResponseEntity<ApiResponse<T>> create(final String path, final T objectToReturn) {
        if (objectToReturn == null) {
            LOGGER.error("Error generating response for null object in path {}", path);
            return createErrorResponse();

        } else {
            return createSuccessfulResponse(
                    path,
                    objectToReturn,
                    HttpStatus.OK
            );
        }
    }

    public <T> ResponseEntity<ApiResponse<T>> createSuccessfulResponse(
            final String path,
            final T objectToReturn,
            final HttpStatus status
    ) {
        return new ResponseBuilder<T>()
                .withStatus(status)
                .withSuccessData(URI.create(path), objectToReturn)
                .build();
    }

    public <T> ResponseEntity<ApiResponse<T>> createErrorResponse() {
        return new ResponseBuilder<T>()
                .withStatus(HttpStatus.NOT_FOUND)
                .withApiError(HttpStatus.NOT_FOUND.toString(), HttpStatus.NOT_FOUND.getReasonPhrase())
                .build();
    }


}
