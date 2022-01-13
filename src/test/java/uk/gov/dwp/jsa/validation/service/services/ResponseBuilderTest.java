package uk.gov.dwp.jsa.validation.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;

import java.net.URI;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

public class ResponseBuilderTest {

    private ResponseBuilder<String> responseBuilder;

    @Before
    public void setup(){
        responseBuilder = new ResponseBuilder<String>();
    }

    @Test
    public void withStatus() {
        ResponseEntity result = responseBuilder.withStatus(HttpStatus.NOT_FOUND).build();
        assertEquals(result.getStatusCode(),HttpStatus.NOT_FOUND);
    }

    @Test
    public void withApiError() {
        ResponseEntity<ApiResponse<String>> result = responseBuilder.withStatus(HttpStatus.NOT_FOUND).withApiError("Code","ERROR").build();
        final ApiResponse<String> body = result.getBody();
        assertThat(body.getError().getCode(),containsString("Code"));
        assertThat(body.getError().getMessage(),containsString("ERROR"));
    }

    @Test
    public void withSuccessData() {
        ResponseEntity<ApiResponse<String>> result = responseBuilder.withStatus(HttpStatus.OK).withSuccessData(URI.create("/uri"),"data").build();
        final ApiResponse<String> body = result.getBody();
        assertThat(body.getSuccess().get(0).getPath().toString(),containsString("/uri"));
        assertThat(body.getSuccess().get(0).getData(),containsString("data"));
    }

}
