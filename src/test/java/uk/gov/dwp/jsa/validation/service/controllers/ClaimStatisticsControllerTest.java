package uk.gov.dwp.jsa.validation.service.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.jsa.adaptors.dto.claim.AgentPerformances;
import uk.gov.dwp.jsa.adaptors.dto.claim.ClaimStatistics;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.validation.service.services.ClaimStatisticsService;
import uk.gov.dwp.jsa.validation.service.services.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClaimStatisticsControllerTest {

    private static final ClaimStatistics CLAIM_STATISTICS = new ClaimStatistics();
    private static final AgentPerformances AGENT_PERFORMANCES = new AgentPerformances();
    private static final String PATH = "PATH";
    private static final ResponseEntity<ApiResponse<ClaimStatistics>> RESPONSE_ENTITY =
            new ResponseBuilder<ClaimStatistics>()
                    .withStatus(HttpStatus.ACCEPTED)
                    .build();
    private static final ResponseEntity<ApiResponse<AgentPerformances>> RESPONSE_ENTITY_AGENT =
            new ResponseBuilder<AgentPerformances>()
                    .withStatus(HttpStatus.ACCEPTED)
                    .build();
    private static final LocalDate today = LocalDate.now();

    @Mock
    private ResponseFactory responseFactory;
    @Mock
    private ClaimStatisticsService claimStatisticsService;
    @Mock
    private HttpServletRequest request;

    private ClaimStatisticsController controller;
    private ResponseEntity<ApiResponse<ClaimStatistics>> responseEntity;
    private ResponseEntity<ApiResponse<AgentPerformances>> responseEntityAgent;

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void getsClaimStatisatics() {
        givenAController();
        withClaimStatisticsMocked();
        whenIGetTheClaimStatistics();
        thenTheClaimStatisticsAreReturned();
    }

    @Test
    public void getAgentPerformance() {
        givenAController();
        withAgentPerformanceMocked();
        whenIGetTheAgentPerformance();
        thenTheAgentPerformancesAreReturned();
    }

    private void givenAController() {
        controller = new ClaimStatisticsController(responseFactory, claimStatisticsService);
    }

    private void withClaimStatisticsMocked() {
        when(claimStatisticsService.getAllClaimStatistics(today)).thenReturn(CLAIM_STATISTICS);
        when(request.getRequestURI()).thenReturn(PATH);
        when(responseFactory.create(PATH, CLAIM_STATISTICS)).thenReturn(RESPONSE_ENTITY);
    }

    private void withAgentPerformanceMocked() {
        when(claimStatisticsService.getAgentPerformances(today, today)).thenReturn(AGENT_PERFORMANCES);
        when(request.getRequestURI()).thenReturn(PATH);
        when(responseFactory.create(PATH, AGENT_PERFORMANCES)).thenReturn(RESPONSE_ENTITY_AGENT);
    }

    private void whenIGetTheClaimStatistics() {
        responseEntity = controller.getClaimStatistics(today, request);
    }

    private void whenIGetTheAgentPerformance() {
        responseEntityAgent = controller.getAgentPerformance(today, today, request);
    }

    private void thenTheClaimStatisticsAreReturned() {
        assertThat(responseEntity, is(RESPONSE_ENTITY));
    }

    private void thenTheAgentPerformancesAreReturned() {
        assertThat(responseEntityAgent, is(RESPONSE_ENTITY_AGENT));
    }

}
