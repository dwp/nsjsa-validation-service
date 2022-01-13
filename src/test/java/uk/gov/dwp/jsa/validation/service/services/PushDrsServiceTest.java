package uk.gov.dwp.jsa.validation.service.services;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.jsa.adaptors.dto.claim.Address;
import uk.gov.dwp.jsa.adaptors.dto.claim.Claimant;
import uk.gov.dwp.jsa.adaptors.dto.claim.ContactDetails;
import uk.gov.dwp.jsa.adaptors.dto.claim.Name;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonSQSAsyncClientBuilder.class, PushDRSService.class})
public class PushDrsServiceTest {
    private AmazonSQSAsync sqsClient;
    private PushDRSService pushDRSService;
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(PushDrsServiceTest.class);

    private void push(final PushDRSService pushDRSService, final Claimant claimant) {
        pushDRSService.push(
                claimant.getNino(),
                claimant.getDateOfBirth(),
                claimant.getName().getLastName(),
                claimant.getName().getFirstName(),
                claimant.getPostalAddress().getPostCode(),
                claimant.getClaimantId());
    }


    @Before
    public void before() {
        PowerMockito.mockStatic(AmazonSQSAsyncClientBuilder.class);
        AmazonSQSAsyncClientBuilder builder = mock(AmazonSQSAsyncClientBuilder.class);
        when(AmazonSQSAsyncClientBuilder.standard()).thenReturn(builder);

        sqsClient = mock(AmazonSQSAsync.class);
        when(builder.build()).thenReturn(sqsClient);
        when(builder.withEndpointConfiguration(any())).thenReturn(builder);
        when(sqsClient.getQueueUrl(anyString())).thenReturn(new GetQueueUrlResult().withQueueUrl("http://hello:8080/resq"));
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(new SendMessageResult().withMessageId("abc123"));

        pushDRSService = new PushDRSService("", "hello");
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(pushDRSService, "claimStatementServiceURL", "http://localhost:8082");
        ReflectionTestUtils.setField(pushDRSService, "claimStatementVersion", "1");

        restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        ReflectionTestUtils.setField(pushDRSService, "restTemplate", restTemplate);
    }

    @Test
    public void happyPathAltConfig() {
        PushDRSService pushDRSService1 = new PushDRSService("qqq", "hello");
        ReflectionTestUtils.setField(pushDRSService1, "claimStatementServiceURL", "http://localhost:8082");
        ReflectionTestUtils.setField(pushDRSService1, "claimStatementVersion", "1");
        ReflectionTestUtils.setField(pushDRSService1, "restTemplate", restTemplate);
        push(pushDRSService1, getClaimant());
        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    public void getPdfFail404() {
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        String result = pushDRSService.getPdf(UUID.randomUUID());
        verify(restTemplate, times(1)).getForEntity(anyString(), any());
        Assert.assertNull(result);
    }

    @Test
    public void getPdfFailException() {
        when(restTemplate.getForEntity(anyString(), any())).thenThrow(new RestClientException("good, but wrong"));
        String result = pushDRSService.getPdf(UUID.randomUUID());
        verify(restTemplate, times(1)).getForEntity(anyString(), any());
        Assert.assertNull(result);
    }

    @Test
    public void happyPathAltConfig2() {
        PushDRSService pushDRSService1 = new PushDRSService("", "");
        push(pushDRSService1, getClaimant());
        verify(sqsClient, times(0)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    public void happyPath() {
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        UUID claimRef = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        pushDRSService.push("AB123456A", date, "Gregson", "Greg", "AA1 1AA", claimRef);

        verify(sqsClient, times(1)).sendMessage(argumentCaptor.capture());
    }

    @Test
    public void checkMetadata() throws Exception {
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        push(pushDRSService, getClaimant());
        verify(sqsClient, times(1)).sendMessage(argumentCaptor.capture());

        PushDRSService.DrsMessage message = objectMapper.readValue(argumentCaptor.getValue().getMessageBody(),
                PushDRSService.DrsMessage.class);
        Assert.assertEquals(11855, message.metadata.documentType.longValue());
        Assert.assertEquals(45, message.metadata.businessUnitID.longValue());
        Assert.assertEquals(66, message.metadata.benefitType.longValue());
    }

    interface IModifyClaimant {
        void modifyClaimant(Claimant claimant);
    }

    private String strip(final String s, int maxLen, String attr) {
        if (s == null) return null;

        String r = s.toUpperCase().replaceAll("_", "");

        if (attr.contains("s")) r = r.replaceAll("\\s", "");
        if (attr.contains("n")) r = r.replaceAll("[0-9]", "");
        if (attr.contains("a")) r = StringUtils.stripAccents(r);

        return r.substring(0, r.length() > maxLen ? maxLen : r.length());
    }

    private void testSubmissionValidation(IModifyClaimant modifyClaimant) throws Exception {
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        Claimant claimant = getClaimant();

        modifyClaimant.modifyClaimant(claimant);

        push(pushDRSService, claimant);
        verify(sqsClient, times(1)).sendMessage(argumentCaptor.capture());

        PushDRSService.DrsMessage message = objectMapper.readValue(argumentCaptor.getValue().getMessageBody(), PushDRSService.DrsMessage.class);
        Assert.assertTrue(drsValidations(message));

        LocalDate parsedDate = LocalDate.parse(message.metadata.dateOfBirth, DateTimeFormatter.ofPattern("yyyyMMdd"));
        Assert.assertTrue(claimant.getDateOfBirth().isEqual(parsedDate));
        Assert.assertEquals(
                strip(claimant.getPostalAddress().getPostCode(), 999, "s"),
                strip(claimant.getPostalAddress().getPostCode(), 999, "s"));
        Assert.assertEquals(strip(claimant.getName().getLastName(), 35, "na"),
                strip(message.metadata.surname, 35, ""));
        Assert.assertEquals(strip(claimant.getName().getFirstName(), 70, "na"),
                strip(message.metadata.forename, 70, ""));
        Assert.assertEquals(strip(claimant.getNino(), 999, "s"),
                message.metadata.nino.ninoBody + message.metadata.nino.ninoSuffix);
    }

    @Test
    public void happyPathWithClaimant() throws Exception {
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        Claimant claimant = getClaimant();

        push(pushDRSService, claimant);
        verify(sqsClient, times(1)).sendMessage(argumentCaptor.capture());

        PushDRSService.DrsMessage message = objectMapper.readValue(argumentCaptor.getValue().getMessageBody(), PushDRSService.DrsMessage.class);
        Assert.assertTrue(drsValidations(message));
    }

    @Test
    public void badCapsNames1() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("smith");
            c.getName().setFirstName("john");
        });
    }

    @Test
    public void badCapsNames2() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("SMITH");
            c.getName().setFirstName("JOHN");
        });
    }

    @Test
    public void doubleBarrelNames() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("Smith-holmes");
            c.getName().setFirstName("Fred");
        });
    }

    @Test
    public void doubleBarrelNamesCaps2() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("Smith-Holmes");
            c.getName().setFirstName("Fred");
        });
    }

    @Test
    public void doubleBarrelNamesSpaces() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("Smith holmes");
            c.getName().setFirstName("Fred");
        });
    }

    @Test
    public void numbersNames() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("Smith33");
            c.getName().setFirstName("Fred1");
        });
    }

    @Test
    public void specialCharacters() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("the_lion-heart");
            c.getName().setFirstName("Fred1");
        });
    }

    private String generateName(int n) {
        final char[] seq = "HELLOMUM".toCharArray();
        char[] name = new char[n];
        for (int i = 0; i < name.length; i++) {
            name[i] = seq[i&7];
        }
        return new String(name);
    }


    @Test
    public void longNames() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName(generateName(40));
            c.getName().setFirstName(generateName(80));
        });
    }

    @Test
    public void accentedNames() throws Exception {
        testSubmissionValidation(c -> {
            c.getName().setLastName("ßîthéñ");
            c.getName().setFirstName("Frédérîçk");
        });
    }

    @Test
    public void noFirstName() throws Exception {
        testSubmissionValidation(c -> c.getName().setFirstName(null));
    }

    @Test
    public void noFirstName2() throws Exception {
        testSubmissionValidation(c -> c.getName().setFirstName(""));
    }

    @Test
    public void noLastName() throws Exception {
        testSubmissionValidation(c -> c.getName().setLastName(null));
    }

    @Test
    public void specialCharactersAllowed() throws Exception {
        testSubmissionValidation(c -> c.getName().setLastName("O\'Dowd"));
    }

    @Test
    public void specialCharactersAllowed2() throws Exception {
        testSubmissionValidation(c -> c.getName().setLastName("s.mouse"));
    }

    @Test
    public void noLastName2() throws Exception {
        testSubmissionValidation(c -> c.getName().setLastName(""));
    }

    @Test
    public void ninoBad1() throws Exception {
        testSubmissionValidation(c -> c.setNino("ab123456a"));
    }

    @Test
    public void ninoNoSuffix() throws Exception {
        testSubmissionValidation(c -> c.setNino("AB123456"));
    }

    @Test
    public void ninoNoSuffix2() throws Exception {
        testSubmissionValidation(c -> c.setNino("AB123456 "));
    }

    @Test
    public void ninoNoSuffix3() throws Exception {
        testSubmissionValidation(c -> c.setNino("AB123456_"));
    }

    @Test
    public void ninoSpaced() throws Exception {
        testSubmissionValidation(c -> c.setNino("AB 12 34 56 A"));
    }

    @Test
    public void dob1() throws Exception {
        testSubmissionValidation(c -> c.setDateOfBirth(LocalDate.of(1988, 1, 25)));
    }

    @Test
    public void dob2() throws Exception {
        testSubmissionValidation(c -> c.setDateOfBirth(LocalDate.of(1988, 1, 3)));
    }

    @Test
    public void postcodes1() throws Exception {
        testSubmissionValidation(c -> c.getPostalAddress().setPostCode("aa1 1aa"));
    }

    @Test
    public void postcodes2() throws Exception {
        testSubmissionValidation(c -> c.getPostalAddress().setPostCode("AA1 1AA"));
    }

    @Test
    public void postcodes3() throws Exception {
        testSubmissionValidation(c -> c.getPostalAddress().setPostCode("AA1A 1AA"));
    }

    @Test
    public void postcodes4() throws Exception {
        testSubmissionValidation(c -> c.getPostalAddress().setPostCode("aa11aa"));
    }

    @Test
    public void postcodes5() throws Exception {
        testSubmissionValidation(c -> c.getPostalAddress().setPostCode(null));
    }

    @Test
    public void postcodes6() throws Exception {
        testSubmissionValidation(c -> c.getPostalAddress().setPostCode(""));
    }

    @Test
    public void phoneNumber1() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber("07890123456"));
    }

    @Test
    public void phoneNumber2() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber("01234567890"));
    }

    @Test
    public void phoneNumber3() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber("+447890123456"));
    }

    @Test
    public void phoneNumber4() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber("00447890123456"));
    }

    @Test
    public void phoneNumber5() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber("0789012345"));
    }

    @Test
    public void phoneNumber6() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber(null));
    }

    @Test
    public void phoneNumber7() throws Exception {
        testSubmissionValidation(c -> c.getContactDetails().setNumber("012345678901234567890123456789"));
    }

    private Claimant getClaimant() {
        Claimant claimant = new Claimant();
        claimant.setClaimantId(UUID.randomUUID());
        claimant.setName(new Name("Mr", "Mark", "Smith"));
        Address address = new Address();
        address.setPostCode("AA1 1AA");
        claimant.setPostalAddress(address);
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setNumber("07890123456");
        claimant.setContactDetails(contactDetails);
        claimant.setNino("AA123456A");
        claimant.setDateOfBirth(LocalDate.now());
        return claimant;
    }

    private boolean drsValidations(PushDRSService.DrsMessage msg) {
        if (!msg.metadata.nino.ninoBody.matches("[A-CEHJ-MOPRSW-Y]{1}[A-CEGHJ-NPR-TW-Z]{1}[0-9]{6}") &&
            !msg.metadata.nino.ninoBody.matches("[\\S\\s\\d]{0,0}") &&
            !msg.metadata.nino.ninoBody.matches("[G]{1}[ACEGHJ-NPR-TW-Z]{1}[0-9]{6}") &&
            !msg.metadata.nino.ninoBody.matches("[N]{1}[A-CEGHJL-NPR-TW-Z]{1}[0-9]{6}") &&
            !msg.metadata.nino.ninoBody.matches("[T]{1}[A-CEGHJ-MPR-TW-Z]{1}[0-9]{6}") &&
            !msg.metadata.nino.ninoBody.matches("[Z]{1}[A-CEGHJ-NPR-TW-Y]{1}[0-9]{6}")) {
            // should allow:
            //[A-CEHJ-MOPRSW-Y]{1}[A-CEGHJ-NPR-TW-Z]{1}[0-9]{6}
            //Allow an empty value to be passed
            //Disallow GBnnnnnn
            //Disallow NKnnnnnn
            //Disallow TNnnnnnn
            //Disallow ZZnnnnnn
            LOGGER.error("Fails NINO body validation");
            return false;
        }

        if (msg.metadata.nino.ninoSuffix != null &&
            !msg.metadata.nino.ninoSuffix.matches("[A-D ]{1}") &&
            !msg.metadata.nino.ninoSuffix.matches("[\\S\\s\\d]{0,0}")) {
            System.out.println("Fails NINO suffix validation");
            return false;
        }

        // all optional, but for our case, should be there
        if (msg.metadata.forename != null &&
            !msg.metadata.forename.matches("([A-Z](([A-Za-z]|'|\\.|-|\\s)?[A-Za-z])*)?") &&
            msg.metadata.forename.length() > 70) {
            System.out.println("Fails forename validation");
            return false;
        }

        if (msg.metadata.surname != null &&
            !msg.metadata.surname.matches("([A-Z](([A-Za-z]|'|\\.|-|\\s)?[A-Za-z])*)?") &&
            msg.metadata.surname.length() > 35) {
            System.out.println("Fails surname validation");
            return false;
        }

        if (msg.metadata.dateOfBirth != null &&
            !msg.metadata.dateOfBirth.matches("\\d{8}") &&
            LocalDate.parse(msg.metadata.dateOfBirth, DateTimeFormatter.ofPattern("CCYYMMDD")) != null) {
            System.out.println("Fails dateOfBirth validation");
            return false;
        }

        if (msg.metadata.surname != null &&
            (!msg.metadata.surname.matches("([A-Z](([A-Za-z]|'|\\.|-|\\s)?[A-Za-z])*)?") ||
            msg.metadata.surname.length() > 35)) {
            System.out.println("Fails surname validation");
            return false;
        }

        if (msg.metadata.postCode != null && msg.metadata.postCode.length() > 8) {
            System.out.println("Fails postcode validation");
            return false;
        }

        if (msg.metadata.benefitType != null && !msg.metadata.benefitType.toString().matches ("\\d{1,4}")) {
            System.out.println("Fails benefitType validation");
            return false;
        }

        if (msg.metadata.documentSource == null && !msg.metadata.documentSource.toString().matches("\\d{1,2}")) {
            System.out.println("Fails documentSource validation");
            return false;
        }

        if (msg.metadata.businessUnitID == null || !msg.metadata.businessUnitID.toString().matches("\\d{1,2}")) {
            System.out.println("Fails businessUnitID validation");
            return false;
        }

        if (msg.metadata.classification == null || !msg.metadata.classification.toString().matches("\\d{1,2}")) {
            System.out.println("Fails classification validation");
            return false;
        }

        if (msg.metadata.documentType == null || !msg.metadata.documentType.toString().matches("\\d{1,10}")) {
            System.out.println("Fails documentType validation");
            return false;
        }

        return true;
    }

}
