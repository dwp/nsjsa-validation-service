package uk.gov.dwp.jsa.validation.service.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.adaptors.RestfulExecutor;
import uk.gov.dwp.jsa.adaptors.ServicesProperties;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.validation.service.models.http.CurrentStatusDto;

import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;

@Service
public class ClaimantServiceClient {

    private static final String CITIZEN_STATUS_URL = "/nsjsa/v%s/citizen/%s/status";
    private final RestfulExecutor restfulExecutor;
    private final ServicesProperties servicesProperties;

    @Autowired
    public ClaimantServiceClient(
            final RestfulExecutor restfulExecutor, final ServicesProperties servicesProperties) {
        this.servicesProperties = servicesProperties;
        this.restfulExecutor = restfulExecutor;
    }

    public void sendLatestStatus(
            final UUID claimantId,
            final CurrentStatusDto currentStatus
    ) {
        validateInputs(claimantId, currentStatus);

        restfulExecutor.put(getUrl(CITIZEN_STATUS_URL, claimantId), currentStatus, ApiResponse.class,
                RestfulExecutor::okOrNotFound);

    }

    private void validateInputs(final UUID claimantId, final CurrentStatusDto currentStatus) {
        Objects.requireNonNull(claimantId);
        Objects.requireNonNull(currentStatus);
        Objects.requireNonNull(currentStatus.getCreatedTimestamp());
    }


    private String getUrl(final String urlTemplate, final UUID id) {
        return servicesProperties.getClaimantServer()
                + format(urlTemplate, servicesProperties.getClaimantVersion(), id);
    }
}
