package uk.gov.dwp.jsa.validation.service.services;


import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;


public class ResponseBuilderTest {

    private ResponseBuilder<String> responseBuilder;

    @Before
    public void setup(){
        responseBuilder = new ResponseBuilder<>();
    }

    @Test
    public void withStatus() {
        final ResponseEntity result = responseBuilder.withStatus(HttpStatus.NOT_FOUND).build();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void withApiError() {
        final ResponseEntity<ApiResponse<String>> result = responseBuilder.withStatus(HttpStatus.NOT_FOUND).withApiError("Code","ERROR").build();
        final ApiResponse<String> body = result.getBody();
        assertThat(body.getError().getCode()).contains("Code");
        assertThat(body.getError().getMessage()).contains("ERROR");
    }

    @Test
    public void withSuccessData() {
        final ResponseEntity<ApiResponse<String>> result = responseBuilder.withStatus(HttpStatus.OK).withSuccessData(URI.create("/uri"),"data").build();
        final ApiResponse<String> body = result.getBody();
        assertThat(body.getSuccess().get(0).getPath().toString()).contains("/uri");
        assertThat(body.getSuccess().get(0).getData()).contains("data");
    }

}
