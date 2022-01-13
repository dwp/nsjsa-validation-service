package uk.gov.dwp.jsa.validation.service.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.adaptors.BankDetailsServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.CircumstancesServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.ClaimantServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.OfficeSearchServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.RestfulExecutor;
import uk.gov.dwp.jsa.adaptors.dto.BenefitCentre;
import uk.gov.dwp.jsa.adaptors.dto.LocalOffice;
import uk.gov.dwp.jsa.adaptors.dto.claim.BankDetails;
import uk.gov.dwp.jsa.adaptors.dto.claim.Claimant;
import uk.gov.dwp.jsa.adaptors.dto.claim.circumstances.Circumstances;
import uk.gov.dwp.jsa.adaptors.dto.claim.validation.PushClaimResponse;
import uk.gov.dwp.jsa.adaptors.dto.jsaps.JsapsRequest;
import uk.gov.dwp.jsa.validation.service.exceptions.ClaimantNotFoundException;
import uk.gov.dwp.jsa.validation.service.models.http.ClaimStatusResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class PushClaimService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushClaimService.class);
    private static final String BLANK = "";

    private final ClaimantServiceAdaptor claimantServiceAdaptor;
    private final CircumstancesServiceAdaptor circumstancesServiceAdaptor;
    private final BankDetailsServiceAdaptor bankDetailsServiceAdaptor;
    private final PushJsapsService pushJsapsService;
    private final ValidationService validationService;
    private final PushIssueService pushIssueService;
    private final OfficeSearchServiceAdaptor officeSearchServiceAdaptor;
    private final PushDRSService pushDRSService;

    @Autowired
    public PushClaimService(final ClaimantServiceAdaptor claimantServiceAdaptor,
                            final CircumstancesServiceAdaptor circumstancesServiceAdaptor,
                            final BankDetailsServiceAdaptor bankDetailsServiceAdaptor,
                            final PushJsapsService pushJsapsService,
                            final ValidationService validationService,
                            final PushIssueService pushIssueService,
                            final OfficeSearchServiceAdaptor officeSearchServiceAdaptor,
                            final PushDRSService pushDRSService
    ) {
        this.claimantServiceAdaptor = claimantServiceAdaptor;
        this.circumstancesServiceAdaptor = circumstancesServiceAdaptor;
        this.bankDetailsServiceAdaptor = bankDetailsServiceAdaptor;
        this.pushJsapsService = pushJsapsService;
        this.validationService = validationService;
        this.pushIssueService = pushIssueService;
        this.officeSearchServiceAdaptor = officeSearchServiceAdaptor;
        this.pushDRSService = pushDRSService;
    }

    public boolean pushClaimDrs(final UUID claimantId) {
        final Optional<Claimant> claimantOptional = claimantServiceAdaptor.getClaimant(claimantId)
                .exceptionally(ex -> RestfulExecutor.claimantExceptionally(ex, claimantId.toString()))
                .join();

        if (!claimantOptional.isPresent()) {
            LOGGER.error("Failed to retrieve claimant details");
            return false;
        }

        final Claimant claimant = claimantOptional.get();
        final String residentialPostcode = claimant.getAddress() == null ? ""
                : claimant.getAddress().getPostCode();
        final String postalPostcode = claimant.getPostalAddress() == null ? ""
                :
                claimant.getPostalAddress().getPostCode();

        return pushDRSService.push(
                claimant.getNino(),
                claimant.getDateOfBirth(),
                claimant.getName().getLastName(),
                claimant.getName().getFirstName(),
                StringUtils.isEmpty(residentialPostcode) ? postalPostcode : residentialPostcode,
                claimant.getClaimantId());
    }

    public PushClaimResponse pushClaim(final UUID claimantId)
            throws ExecutionException, InterruptedException {

        final FullClaimDetails fullClaimDetails = getFullClaimDetails(claimantId);

        if (fullClaimDetails.isClaimIncomplete()) {
            throw new ClaimantNotFoundException();
        }

        PushClaimResponse pushClaimResponse = pushClaimResponseWithAllValuesFalse();


        final String postCode = fullClaimDetails.claimant() != null
                ? fullClaimDetails.claimant().getAddress().getPostCode()
                : "";

        final LocalOfficeIds localOfficeIds = getLocalOfficeIds(postCode);

        final JsapsRequest jsapsRequest = createJsapsRequest(
                fullClaimDetails,
                localOfficeIds
        );

        pushJsapsService.pushToJsaps(jsapsRequest);

        return pushClaimResponse;
    }

    private FullClaimDetails getFullClaimDetails(final UUID claimantId)
            throws ExecutionException, InterruptedException {

        final ClaimStatusResponse claimStatusResponse = validationService.getClaimStatusByClaimantId(claimantId);

        final CompletableFuture<Optional<Claimant>> claimantCall =
                claimantServiceAdaptor.getClaimant(claimantId)
                        .exceptionally(ex -> RestfulExecutor.claimantExceptionally(ex, claimantId.toString()));

        final CompletableFuture<Optional<Circumstances>> circumstancesCall =
                circumstancesServiceAdaptor.getCircumstancesByClaimantId(claimantId)
                        .exceptionally(ex -> RestfulExecutor.circumstancesExceptionally(ex, claimantId.toString()));

        final CompletableFuture<Optional<BankDetails>> bankDetailsCall =
                bankDetailsServiceAdaptor.getBankDetailsByClaimantId(claimantId)
                        .exceptionally(ex -> RestfulExecutor.bankDetailsExceptionally(ex, claimantId.toString()));


        CompletableFuture.allOf(
                claimantCall,
                circumstancesCall,
                bankDetailsCall
        ).join();

        return new FullClaimDetails(
                claimStatusResponse,
                claimantCall.get(),
                circumstancesCall.get(),
                bankDetailsCall.get()
        );
    }

    private JsapsRequest createJsapsRequest(
            final FullClaimDetails fullClaimDetails,
            final LocalOfficeIds localOfficeIds) {

        JsapsRequest request = new JsapsRequest();
        request.setClaimant(fullClaimDetails.claimant());
        request.setCircumstances(fullClaimDetails.circumstances());

        request.setAgentMode(fullClaimDetails.claimStatusResponse.isClaimCreatedByAgent());

        request.setHomeOfficeId(localOfficeIds.jobCentreId);
        request.setTargetOfficeId(localOfficeIds.benefitCentreId);
        request.setEsjNumber(localOfficeIds.esjNumber);

        fullClaimDetails.bankDetails().ifPresent(request::setBankDetails);

        return request;
    }



    private LocalOfficeIds getLocalOfficeIds(final String postCode)
            throws InterruptedException, ExecutionException {
        return officeSearchServiceAdaptor.getLocalOffice(postCode)
                .get()
                .map(LocalOfficeIds::new)
                .orElse(new LocalOfficeIds(null));
    }


    private PushClaimResponse pushClaimResponseWithAllValuesFalse() {
        PushClaimResponse pushResponse = new PushClaimResponse();
        pushResponse.setCorrespondanceAddressUpdateSuccess(false);
        pushResponse.setPostalAddressUpdateSuccess(false);
        pushResponse.setNameUpdateSuccess(false);
        pushResponse.setContactUpdateSuccess(false);
        return pushResponse;
    }


    private static final class FullClaimDetails {
        private final ClaimStatusResponse claimStatusResponse;
        private final Claimant claimant;
        private final Circumstances circumstances;
        private final BankDetails bankDetails;

        private FullClaimDetails(
                final ClaimStatusResponse claimStatusResponse,
                final Optional<Claimant> claimantOptional,
                final Optional<Circumstances> circumstancesOptional,
                final Optional<BankDetails> bankDetailsOptional
        ) {
            this.claimStatusResponse = claimStatusResponse;
            this.claimant = claimantOptional.orElse(null);
            this.circumstances = circumstancesOptional.orElse(null);
            this.bankDetails = bankDetailsOptional.orElse(null);
        }

        private boolean isClaimComplete() {
            return Optional.ofNullable(claimant).isPresent()
                    && Optional.ofNullable(circumstances).isPresent();
        }

        private boolean isClaimIncomplete() {
            return !isClaimComplete();
        }

        private Claimant claimant() {
            return claimant;
        }

        private Circumstances circumstances() {
            return circumstances;
        }


        private Optional<BankDetails> bankDetails() {
            return Optional.ofNullable(bankDetails);
        }

    }


    private static final class LocalOfficeIds {
        private final String jobCentreId;
        private final String benefitCentreId;
        private final String esjNumber;

        private LocalOfficeIds(final LocalOffice localOffice) {
            final Optional<LocalOffice> localOfficeOptional = Optional.ofNullable(localOffice);

            jobCentreId = localOfficeOptional
                    .map(LocalOffice::getJobCentreId)
                    .orElse(BLANK);

            esjNumber = Optional.of(jobCentreId)
                    .map(id -> StringUtils.substring(id, 1))
                    .orElse(BLANK);

            benefitCentreId = localOfficeOptional
                    .map(LocalOffice::getBenefitCentre)
                    .map(BenefitCentre::getBenefitCentreId)
                    .orElse(BLANK);
        }
    }

}
