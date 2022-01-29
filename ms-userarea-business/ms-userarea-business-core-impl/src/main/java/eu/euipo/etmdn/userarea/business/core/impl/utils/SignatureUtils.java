/*
 * $Id:: SignatureUtils.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.utils;

import eu.euipo.etmdn.userarea.business.core.impl.comparator.SignatureComparator;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.signature.FOSignatureResource;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortColumn;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortingCriteriaRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ASCENDING;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TRADEMARK;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.UA_DS_EFILING;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.UA_TM_EFILING;

/**
 * The SignatureUtils class.
 */
@Slf4j
public class SignatureUtils {


    private SignatureUtils() { }

    public static void sortSignatures(SignatureSortingCriteriaRequest signatureSortingCriteriaRequest, List<Signature> signatureList) {
        if (signatureSortingCriteriaRequest.getSortColumn() == SignatureSortColumn.TYPE) {
            if(signatureSortingCriteriaRequest.getSortType().equalsIgnoreCase(ASCENDING) ) {
                signatureList.sort(new SignatureComparator(SignatureSortColumn.TYPE));
            } else {
                signatureList.sort(new SignatureComparator(SignatureSortColumn.TYPE).reversed());
            }
        } else if(signatureSortingCriteriaRequest.getSortColumn() == SignatureSortColumn.APPLICATION) {
            if(signatureSortingCriteriaRequest.getSortType().equalsIgnoreCase(ASCENDING) ) {
                signatureList.sort(new SignatureComparator(SignatureSortColumn.APPLICATION));
            } else {
                signatureList.sort(new SignatureComparator(SignatureSortColumn.APPLICATION).reversed());
            }
        } else if(signatureSortingCriteriaRequest.getSortColumn() == SignatureSortColumn.SIGNEDAT) {
            if(signatureSortingCriteriaRequest.getSortType().equalsIgnoreCase(ASCENDING)) {
                signatureList.sort(new SignatureComparator(SignatureSortColumn.SIGNEDAT).reversed());
            } else {
                signatureList.sort(new SignatureComparator(SignatureSortColumn.SIGNEDAT));
            }
        }
    }

    public static String getApplicationType(Application application) {
        if (ApplicationType.ESERVICE.value.equalsIgnoreCase(application.getFoModule())) {
            return application.getEserviceName();
        } else {
            return application.getFoModule().equalsIgnoreCase(TRADEMARK) ? UA_TM_EFILING : UA_DS_EFILING;
        }
    }

    public static boolean shouldBeDisplayedInSignatures(final ApplicationConfiguration applicationConfiguration,
                                                        final String applicationStatus, int signatureListSize) {
        return applicationConfiguration.getStatus().getSignature().equalsIgnoreCase(applicationStatus) ||
                (applicationConfiguration.getStatus().getPayment().equalsIgnoreCase(applicationStatus) &&
                        (signatureListSize > 0  && signatureListSize < applicationConfiguration.getSignature().getMaxSignatories()));
    }

    public static void deleteSignaturesInFO(String applicationNumber, RestTemplate restTemplate,
                                            String frontOfficeUrl, String frontOfficeSignatureDeleteEndpoint) {
        // Make an async api call to frontOffice in order to delete signatures
        CompletableFuture
                .supplyAsync(() -> deleteFOSignatures(applicationNumber, restTemplate, frontOfficeUrl, frontOfficeSignatureDeleteEndpoint))
                .thenAccept(result -> {
                    // If we cannot notify frontOffice update signature status (the status is considered completed)
                    if (result) {
                        log.info("FrontOffice delete signatures success");
                    } else {
                        log.error("FrontOffice could not be updated");
                    }
                });
    }

    /**
     * Deletes all signatures from xml saved in frontoffice.
     *
     * @param applicationNumber an application number
     * @return true if successful
     */
    private static boolean deleteFOSignatures(String applicationNumber, RestTemplate restTemplate,
                                              String frontOfficeUrl, String frontOfficeSignatureDeleteEndpoint) {
        FOSignatureResource payload = new FOSignatureResource();
        // FrontOffice supports the deletion of multiple applications at the same time.
        // So for one application we send a list with one item.
        payload.setApplicationNumbers(Collections.singletonList(applicationNumber));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FOSignatureResource> entity = new HttpEntity<>(payload, headers);
        // Delete all signatures of an application is frontOffice
        try {
            log.info(">>> Notifying delete signatures to frontOffice for applicationNumber " + applicationNumber);
            restTemplate.exchange(frontOfficeUrl + frontOfficeSignatureDeleteEndpoint, HttpMethod.DELETE, entity, String.class);
        } catch (RestClientException ex) {
            log.error(">>> Could not notify delete signatures to frontOffice: " + ex.getLocalizedMessage());
            return false;
        }
        log.info(">>> FrontOffice updated");
        return true;
    }
}
