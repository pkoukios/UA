/*
 * $Id:: UserProfileServiceImpl.java 2021/03/01 09:07 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.business.core.impl.service;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.business.core.api.service.SignatureService;
import eu.euipo.etmdn.userarea.business.core.impl.mapper.ApplicationMapper;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.exception.SignatureClientException;
import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.signature.SignatureEntity;
import eu.euipo.etmdn.userarea.common.persistence.repository.SignatureRepository;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.signature.FOSignatureResource;
import eu.euipo.etmdn.userarea.domain.signature.PlatformSignatureDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;
import eu.euipo.etmdn.userarea.domain.signature.SignResource;
import eu.euipo.etmdn.userarea.domain.signature.SignatureCallbackResource;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortingCriteriaRequest;
import eu.euipo.etmdn.userarea.external.signature.api.client.SignatureClient;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static eu.euipo.etmdn.userarea.business.core.impl.utils.SignatureUtils.deleteSignaturesInFO;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.SignatureUtils.getApplicationType;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.SignatureUtils.shouldBeDisplayedInSignatures;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.SignatureUtils.sortSignatures;

/**
 * The Signature services implementation.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Service
@Transactional
public class SignatureServiceImpl implements SignatureService {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ApplicationConfiguration applicationConfiguration;
    @Autowired
    private SignatureClient signatureClient;
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    @Lazy
    private ShoppingCartService shoppingCartService;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${userarea.signature.platform.url}")
    private String platformUrl;
    @Value("${userarea.signature.callback.url}")
    private String callbackUrl;
    @Value("${userarea.globals.ipo.fo.url}")
    private String frontofficeUrl;
    @Value("${userarea.signature.frontoffice.updateEndpoint}")
    private String frontofficeUpdateEndpoint;
    @Value("${userarea.signature.frontoffice.deleteEndpoint}")
    private String frontofficeSignatureDeleteEndpoint;

    /**
     * Retrieve signatures.
     *
     * @param username                        the username
     * @param roles                           the set with account roles
     * @param signatureSortingCriteriaRequest the signature sorting criteria request
     * @return {@link List<Signature>} the list signatures
     */
    @Override
    public List<Signature> getSignatures(final String username, final Set<String> roles, final SignatureSortingCriteriaRequest signatureSortingCriteriaRequest) {
        List<Application> applicationList = applicationService.getApplicationsForSignatures(username, roles);
        List<Signature> signatureList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(applicationList)) {
            for (Application application : applicationList) {
                List<SignatureEntity> signatureEntities = application.getSignatures().stream()
                        .filter(signature -> applicationConfiguration.getSignature().getStatus().getCompleted().equalsIgnoreCase(signature.getStatus())
                        || applicationConfiguration.getSignature().getStatus().getCompletedUpdateFoFailed().equalsIgnoreCase(signature.getStatus()))
                        .collect(Collectors.toList());
                if (shouldBeDisplayedInSignatures(applicationConfiguration, application.getStatus(), signatureEntities.size())) {
                    Collections.reverse(signatureEntities);
                    Signature signature = new Signature();
                    signature.setType(getApplicationType(application));
                    signature.setNumber(application.getNumber());
                    signature.setName(signatureEntities.stream().map(SignatureEntity::getName).collect(Collectors.joining(",")));
                    signature.setCapacity(signatureEntities.stream().map(SignatureEntity::getCapacity).collect(Collectors.joining(",")));
                    signature.setDate(signatureEntities.stream().map(SignatureEntity::getDate).map(LocalDateTime::toString).collect(Collectors.joining(",")));
                    signatureList.add(signature);
                }
            }
            sortSignatures(signatureSortingCriteriaRequest, signatureList);
        }
        return signatureList;
    }

    /**
     * Delete application.
     *
     * @param username          the username
     * @param applicationNumber the applicationNumber
     */
    @Override
    public String deleteApplication(String username, String applicationNumber) {
        log.info("Deleting application from signature");
        this.deleteByApplicationNumber(username, applicationNumber);
        applicationService.deleteApplication(username, applicationNumber);
        return StringUtils.EMPTY;
    }

    /**
     * Modify application.
     *
     * @param username          the username
     * @param applicationNumber the applicationNumber
     * @return the resume url
     */
    @Override
    public String modifyApplication(String username, String applicationNumber) {
        log.info("Modify application from signature");
        this.deleteByApplicationNumber(username, applicationNumber);
        return applicationService.modifyApplication(username, applicationNumber);
    }

    /**
     * Validates a signature request, sends the request data to external platform,
     * saves platform response (signatureId and redirectUrl) to database
     *
     * @param username    the username
     * @param signRequest the sign request
     * @return {@link SignResource} the sign resource
     */
    @Override
    public SignResource processSignature(final String username, SignRequest signRequest) {
        log.info("Contacting external signature service");
        String signatureId = createSignatureId(signRequest);
        // Create a SignatureEntity object
        log.info("Saving signature request to DB");
        saveSignRequest(username, signRequest, signatureId);
        return SignResource.builder()
                .signatureId(signatureId)
                .redirectToExternalPlatformUrl(getRedirectToExternalPlatformUrl(signatureId))
                .build();
    }

    /**
     * Save all data related to the signature. Used at the initiation phase.
     *
     * @param signRequest Contains all the Signatory details, applications and callback url
     * @param signatureId the signature id
     */
    private void saveSignRequest(final String username, SignRequest signRequest, String signatureId) {
        List<Application> signatureApplications = applicationService.getApplicationsByNumber(new ArrayList<>(signRequest.getApplicationIds()));
        signatureApplications.forEach(application -> {
            Application app = applicationService.getByIdAndLock(application.getId(), username);
            SignatureEntity signatureEntity = SignatureEntity.builder()
                    .application(app)
                    .name(signRequest.getSignatoryDetails().getFullName())
                    .capacity(signRequest.getSignatoryDetails().getCapacity())
                    .email(signRequest.getSignatoryDetails().getEmail())
                    .number(app.getNumber())
                    .username(username)
                    .reference(signatureId)
                    .deleted(false)
                    .status(applicationConfiguration.getSignature().getStatus().getPending())
                    .build();
            signatureRepository.save(signatureEntity);
            app.getSignatures().add(signatureEntity);
            applicationService.save(app);
        });
    }

    /**
     * Create and retrieve a signature id from the external signature platform
     *
     * @param signRequest Request from a signatory to sign a list of applications
     * @return the platform signature id
     */
    @Override
    public String createSignatureId(SignRequest signRequest) {
        // Make call to external service
        signRequest.setCallbackUrl(callbackUrl);
        String signatureId = signatureClient.createSignatureId(signRequest);
        if (signatureId != null) {
            return signatureId;
        } else {
            throw new SignatureClientException("External Platform Error");
        }
    }

    private String getRedirectToExternalPlatformUrl(String signatureId) {
        return String.join("/", platformUrl, "signatures", "register", signatureId);
    }

    /**
     * Confirm a signature request. It is called by the callback that the external signature platform makes async to Userarea.
     *
     * @param signatureCallbackResource Details about the signature and the status.
     */
    @Override
    public void confirm(SignatureCallbackResource signatureCallbackResource) {
        log.info("Confirmation from signature platform: signatureId: " + signatureCallbackResource.getSignatureId() +
                " signingId: " + signatureCallbackResource.getSigningId() +
                " status: " + signatureCallbackResource.getStatus());
        List<SignatureEntity> signatureEntities = signatureRepository.findAllByReferenceAndDeleted(signatureCallbackResource.getSignatureId(), false);

        if (signatureEntities != null && !signatureEntities.isEmpty()) {
            signatureEntities.forEach(e -> {
                Application application = e.getApplication();
                e.setStatus(signatureCallbackResource.getStatus());
                e.setSigningId(signatureCallbackResource.getSigningId());
                e.setDate(signatureCallbackResource.getSignedAt());
                signatureRepository.save(e);
                if (applicationConfiguration.getSignature().getStatus().getCompleted().equalsIgnoreCase(e.getStatus())
                        || applicationConfiguration.getSignature().getStatus().getCompletedUpdateFoFailed().equalsIgnoreCase(e.getStatus())) {
                    application.setStatus(applicationConfiguration.getStatus().getPayment());
                    application.setLastModifiedDate(LocalDateTime.now());
                    applicationService.save(application);
                }
                log.info("Updating shopping cart by adding application");
                shoppingCartService.checkAndAddApplicationToShoppingCart(application.getMainAccount(), application, signatureEntities.get(0).getUsername());
                log.info("Confirmation saved to local db");

                CompletableFuture
                        .supplyAsync(() -> notifySignatureToFrontoffice(signatureEntities))
                        .thenAccept(result -> {
                            // If we cannot notify frontoffice update signature status (the status is considered completed)
                            if (!result) {
                                signatureEntities.forEach(signature -> {
                                    signature.setStatus(applicationConfiguration.getSignature().getStatus().getCompletedUpdateFoFailed());
                                    signatureRepository.save(signature);
                                });
                            }
                        });
            });
        }
    }

    private boolean notifySignatureToFrontoffice(List<SignatureEntity> signatures) {
        FOSignatureResource payload = new FOSignatureResource();
        List<String> applicationNumbers = signatures.stream().map(SignatureEntity::getNumber).collect(Collectors.toList());
        SignatureEntity firstSignature = signatures.get(0);

        payload.setFullName(firstSignature.getName());
        payload.setCapacity(firstSignature.getCapacity());
        payload.setEmail(firstSignature.getEmail());
        payload.setSignatureId(firstSignature.getSigningId());
        payload.setApplicationNumbers(applicationNumbers);
        // Update signature in frontoffice
        try {
            log.info(">>> Notifying signatures to frontoffice for applicationNumbers: " + String.join(",", applicationNumbers));
            HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);
            restTemplate.postForObject(frontofficeUrl + frontofficeUpdateEndpoint, payload, String.class);
        } catch (RestClientException ex) {
            log.error(">>> Could not notify new signature to frontoffice: " + ex.getLocalizedMessage());
            return false;
        }

        log.info(">>> Frontoffice updated");
        return true;
    }


    /**
     * Retrieve all applications that have the same signature
     *
     * @param signatureId Unique identifier for the signature
     * @return A list of {@link ApplicationDetails}
     */
    @Override
    public List<ApplicationDetails> getAllApplicationDetailsBy(String signatureId) {
        List<Application> applications = getApplicationsBySignatureId(signatureId);
        return ApplicationMapper.MAPPER.toApplicationDetails(applications);
    }

    private List<Application> getApplicationsBySignatureId(String signatureId) {
        List<SignatureEntity> signatures = signatureRepository.findAllByReferenceAndDeleted(signatureId, false);
        return signatures.stream()
                .map(SignatureEntity::getApplication)
                .collect(Collectors.toList());
    }

    /**
     * Find all applications that do not have the maximum number of signatures applied to them
     * and belong to a specific signature request
     *
     * @param signatureId the signature Id
     * @return A list of {@link ApplicationDetails}
     */
    @Override
    public List<String> getNonFullySignedApplicationDetailsBySignatureId(final String username, String signatureId) {
        List<Application> applications = getApplicationsBySignatureId(signatureId);
        return applications.stream()
                .filter(a -> getCompletedSignaturesFor(a.getId()).size() < applicationConfiguration.getSignature().getMaxSignatories())
                .map(Application::getNumber)
                .collect(Collectors.toList());
    }

    private List<Signature> getCompletedSignaturesFor(Long applicationId) {
        List<SignatureEntity> signatureEntities = signatureRepository.findAllByApplicationIdAndStatusAndDeleted(applicationId,
                applicationConfiguration.getSignature().getStatus().getCompleted(), false);
        return signatureEntities.stream().map(signatureEntity -> Signature.builder()
                .name(signatureEntity.getName())
                .capacity(signatureEntity.getCapacity())
                .number(signatureEntity.getCapacity())
                .date(signatureEntity.getDate().toString())
                .build()).collect(Collectors.toList());
    }

    /**
     * Retrieves the signature details including the status from the external signature platform
     *
     * @param signatureId The unique identifier for a signature
     * @return {@link PlatformSignatureDetails} The signature details
     */
    @Override
    public PlatformSignatureDetails getSignatureDetailsFromPlatform(String signatureId) {
        log.info("Retrieve Signature details from Signature Platdform");
        PlatformSignatureDetails platformSignatureDetails = signatureClient.getSignatureDetails(signatureId);
        // Set valid flag to true if the signature is COMPLETED
        boolean signatureIsValid = platformSignatureDetails.getStatus().equals(applicationConfiguration.getSignature().getStatus().getCompleted());
        platformSignatureDetails.setValid(signatureIsValid);
        if (!signatureIsValid) {
            platformSignatureDetails.setErrorCode(LiteralConstants.SIGNATURE_NOT_ADDED_DUE_TO);
        }
        return platformSignatureDetails;
    }

    /**
     * Deletes all signatures of a given application
     *
     * @param applicationNumber the unique identifier of the application
     */
    @Override
    public void deleteByApplicationNumber(final String username, String applicationNumber) {
        log.info("Deleting signatures");
        List<Application> applications = applicationService.getApplicationsByNumberAndLock(applicationNumber, username);
        if (!CollectionUtils.isEmpty(applications)) {
            for (Application application : applications) {
                List<SignatureEntity> signatures = application.getSignatures();
                signatures.forEach(signature -> signature.setDeleted(true));
                signatureRepository.saveAll(signatures);
                application.setSignatures(signatures);
                application.setStatus(applicationConfiguration.getStatus().getSignature());
                log.info("Updating shopping cart by removing application");
                shoppingCartService.removeApplication(application.getId());
            }
            applicationService.saveAll(applications);
            deleteSignaturesInFO(applicationNumber, restTemplate, frontofficeUrl, frontofficeSignatureDeleteEndpoint);
        }
    }
}
