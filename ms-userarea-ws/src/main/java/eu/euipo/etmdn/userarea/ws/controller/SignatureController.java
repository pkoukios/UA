/*
 * $Id:: SignatureController.java 2021/05/13 01:46 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intellectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.SignatureService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationResponse;
import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.domain.signature.PlatformSignatureDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;
import eu.euipo.etmdn.userarea.domain.signature.SignResource;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortingCriteriaRequest;
import eu.euipo.etmdn.userarea.ws.domain.signature.SignatureResource;
import eu.euipo.etmdn.userarea.ws.mapper.signature.SignatureResourceMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The Signature controller.
 */
@Slf4j
@RestController
@AllArgsConstructor
@Secured({"ROLE_SIGNATURES","ROLE_ADMINISTRATOR"})
@RequestMapping("/signatures")
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping
    public ResponseEntity<List<SignatureResource>> getSignatures(Authentication authentication, @Valid @RequestBody SignatureSortingCriteriaRequest signatureSortingCriteriaRequest) {
        log.info("Retrieve all signatures");
        List<Signature> signatures = signatureService.getSignatures(authentication.getName(), AuthorityUtils.authorityListToSet(authentication.getAuthorities()), signatureSortingCriteriaRequest);
        List<SignatureResource> resource = SignatureResourceMapper.MAPPER.mapSignatureResources(signatures);
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/delete/application/{applicationType}/{applicationNumber}")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or (hasRole('ROLE_DESIGNS') and #applicationType == 'design')")
    public ResponseEntity<Void> deleteApplication(Authentication authentication, @PathVariable String applicationType, @PathVariable String applicationNumber) {
        log.info("Delete application from signatures");
        signatureService.deleteApplication(authentication.getName(), applicationNumber);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/modify/application/{applicationType}/{applicationNumber}")
    @PreAuthorize("(hasRole('ROLE_TRADEMARKS') and #applicationType == 'trademark') or (hasRole('ROLE_DESIGNS') and #applicationType == 'design')")
    public ResponseEntity<ApplicationResponse> modifyApplication(Authentication authentication, @PathVariable String applicationType, @PathVariable String applicationNumber) {
        log.info("Modify application from signatures");
        String resumeUrl = signatureService.modifyApplication(authentication.getName(), applicationNumber);
        return ResponseEntity.ok(ApplicationResponse.builder().applicationNumber(applicationNumber).resumeUrl(resumeUrl).build());
    }

    /**
     * Initiates the signing flow for a list of applications. Communicates with the external platform to
     * in order to get a signature id that later will be used to redirect the user to the external platform.
     *
     * @param signRequest the signature request
     * @return SignResource
     */
    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<SignResource> initiateSign(Authentication authentication, @Valid @RequestBody SignRequest signRequest) {
        log.info("Sign application");
        SignResource signingResult = signatureService.processSignature(authentication.getName(), signRequest);
        log.debug("Signature redirect:[{}]", signingResult.getRedirectToExternalPlatformUrl());
        return ResponseEntity.ok(signingResult);
    }

    /**
     * Gets  details about a signatureId directly from the signature platform
     *
     * @param signatureId The unique identifier for a signature request
     * @return {@link PlatformSignatureDetails} All the information about a signature and it's status
     */
    @GetMapping("/detailsFromPlatform/{signatureId}")
    public ResponseEntity<PlatformSignatureDetails> getDetailsFromPlatform(@PathVariable @NotNull String signatureId) {
        log.info("Get signature details from  the signature platform");
        PlatformSignatureDetails signatureDetails = signatureService.getSignatureDetailsFromPlatform(signatureId);
        return ResponseEntity.ok(signatureDetails);
    }

    /**
     * Find which of the applications that belong to the signature request can have more signatures added
     *
     * @param signatureId The unique identifier for a signature request
     * @return List of applicationNumbers
     */
    @GetMapping("/checkAddMore/{signatureId}")
    public ResponseEntity<List<String>> checkAddMore(Authentication authentication, @PathVariable @NotNull String signatureId) {
        log.info("Retrieve the applications that may have more signatures added");
        List<String> applications = signatureService.getNonFullySignedApplicationDetailsBySignatureId(authentication.getName(), signatureId);
        return ResponseEntity.ok(applications);
    }

    /**
     * Delete all signatures of an application
     *
     * @param applicationNumber The unique identifier of an application (Application.Number)
     * @return A response entity with 200 status on delete success or 404 if application could not be found
     */
    @DeleteMapping("/{applicationNumber}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable @NotNull String applicationNumber) {
        log.info("Delete signatures for an application");
        signatureService.deleteByApplicationNumber(authentication.getName(), applicationNumber);
        return ResponseEntity.ok().build();
    }
}