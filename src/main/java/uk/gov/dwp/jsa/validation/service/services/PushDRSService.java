package uk.gov.dwp.jsa.validation.service.services;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

@SuppressFBWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON", "URF_UNREAD_FIELD"})
@Service
public class PushDRSService {
    private static final int BUSINESS_UNIT_ID = 45;
    private static final int CLASSIFICATION = 0;
    private static final int DOCUMENT_TYPE = 11855;
    private static final int DOCUMENT_SOURCE = 4;
    private static final int BENEFIT_TYPE = 66;
    private static final int NINO_SUFFIX_POS = 8;
    private static final int MAX_SURNAME = 35;
    private static final int MAX_FORENAME = 70;


    private static final Logger LOGGER = LoggerFactory.getLogger(PushDRSService.class);
    private AmazonSQSAsync client;
    private ObjectMapper objectMapper;
    private String queueUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${drs.sqs.host:}")
    private String sqsHost;

    @Value("${drs.request.queue:}")
    private String drsRequestQueue;

    @Value("${services.claim-statement-server}")
    private String claimStatementServiceURL;

    @Value("${services.claim-statement-version}")
    private String claimStatementVersion;

    public PushDRSService(@Value("${drs.sqs.host:}") final String sqsHost,
                          @Value("${drs.request.queue:}") final String drsRequestQueue) {
        this.sqsHost = sqsHost;
        this.drsRequestQueue = drsRequestQueue;
        initSQSclient();
    }

    public String getPdf(final UUID claimantId) {
        LOGGER.debug("Getting claim statement for claimant: {}", claimantId);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    claimStatementServiceURL + "/nsjsa/v" + claimStatementVersion
                            + "/claim-statement/claimpdf/" + claimantId.toString(),
                    String.class);
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            LOGGER.error("Error while getting claim from statement service for claimant: {}", claimantId, e);
        }
        return null;
    }

    private void initSQSclient() {
        if (!StringUtils.isNullOrEmpty(drsRequestQueue)) {
            AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
            if (StringUtils.isNullOrEmpty(sqsHost)) {
                builder.withRegion("eu-west-2");
            } else {
                builder.withRequestHandlers(new RequestHandler2() { });
                builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        sqsHost, "eu-west-2"))
                        .withCredentials(new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials("", "")));
            }

            client = builder.build();
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            queueUrl = client.getQueueUrl(drsRequestQueue).getQueueUrl();
            LOGGER.info("Connected to SQS DRS request queue.");
        } else {
            LOGGER.info("No SQS config, not using DRS push.");
        }
    }

    public boolean push(final String nino, final LocalDate dateOfBirth, final String surname,
                     final String forename, final String postCode, final UUID claimId) {

        boolean result = false;

        if (client != null) {
            DrsMessage message = new DrsMessage(sanitiseNino(nino), dateOfBirth,
                    sanitiseName(surname, MAX_SURNAME),
                    sanitiseName(forename, MAX_FORENAME),
                    postCode, claimId, getPdf(claimId));
            String correlationId = UUID.randomUUID().toString();

            try {
                SendMessageResult result1 = client.sendMessage(
                        new SendMessageRequest(queueUrl, objectMapper.writeValueAsString(message))
                        .withMessageAttributes(Collections.singletonMap("JMS_CORRELATION_ID",
                                new MessageAttributeValue().withStringValue(correlationId).withDataType("String"))));

                result = !StringUtils.isNullOrEmpty(result1.getMessageId());

            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to serialise DRS message", e);
            }
        } else {
            LOGGER.error("No client set up to push to DRS");
        }
        return result;
    }

    private String sanitiseName(final String name, final int maxLen) {
        if (name == null) {
            return null;
        }
        String sanitisedName = org.apache.commons.lang3.StringUtils.stripAccents(name)
                .toUpperCase(Locale.ENGLISH)
                .replaceAll("[^A-Z\\-\'.\\s]", "");
        return sanitisedName.length() > maxLen ? sanitisedName.substring(0, maxLen) : sanitisedName;
    }

    private String sanitiseNino(final String nino) {
        return nino.toUpperCase(Locale.ENGLISH).replaceAll("[^A-Z0-9]", "");
    }

    static class DrsMessage {
        static class Metadata {
            @JsonProperty Integer businessUnitID = BUSINESS_UNIT_ID;
            @JsonProperty Integer classification = CLASSIFICATION;
            @JsonProperty Nino nino;
            @JsonProperty Integer documentType = DOCUMENT_TYPE;
            @JsonProperty Integer documentSource = DOCUMENT_SOURCE;
            @JsonProperty String dateOfBirth;
            @JsonProperty String surname;
            @JsonProperty String forename;
            @JsonProperty String postCode;
            @JsonProperty Integer benefitType = BENEFIT_TYPE;

            static class Nino {
                @JsonProperty String ninoBody;
                @JsonProperty String ninoSuffix;
            }

            Metadata() {
                nino = new Nino();
            }
        }

        @JsonProperty Metadata metadata;
        @JsonProperty String payload;

        DrsMessage() {
        }

        DrsMessage(final String nino, final LocalDate dateOfBirth, final String surname,
                   final String forename, final String postCode, final UUID claimId,
                   final String pdf) {
            metadata = new Metadata();
            metadata.nino.ninoBody = nino.substring(0, NINO_SUFFIX_POS);
            metadata.nino.ninoSuffix = nino.substring(NINO_SUFFIX_POS);
            metadata.dateOfBirth = dateOfBirth.format(DateTimeFormatter.BASIC_ISO_DATE);
            metadata.surname = surname;
            metadata.forename = forename;
            metadata.postCode = postCode;

            payload = pdf;
            }
    }
}
