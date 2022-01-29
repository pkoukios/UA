/*
 * $Id:: SignatureService.java 2021/03/09 02:06 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.api.service;

import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.signature.PlatformSignatureDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;
import eu.euipo.etmdn.userarea.domain.signature.SignResource;
import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.domain.signature.SignatureCallbackResource;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortingCriteriaRequest;

import java.util.List;
import java.util.Set;

/**
 * Signature Interface for internal handling of signatures
 */
public interface SignatureService {

    /**
     * Retrieve signatures.
     *
     * @param username                        the username
     * @param roles                           the set with account roles
     * @param signatureSortingCriteriaRequest the signature sorting criteria request
     * @return {@link List<Signature>} the list signatures
     */
    List<Signature> getSignatures(final String username, final Set<String> roles, final SignatureSortingCriteriaRequest signatureSortingCriteriaRequest);

    /**
     * Retrieves a signature id from external signature service.
     *
     * @param signRequest The payload to be sent to the external signature platform. Contains application and Signatory data
     *
     * @return String the platform signature id
     */
    String createSignatureId(SignRequest signRequest);

    /**
     * Validates and sends the sign request to the external signature platform.
     *
     * @param signRequest the sign signature request
     * @return {@link SignResource}
     */
    SignResource processSignature(final String username, SignRequest signRequest);

    /**
     * Confirm a signature request. It is called by the callback that the external signature platform makes async to Userarea.
     *
     * @param signatureCallbackResource  Details about the signature and the status.
     */
    void confirm(SignatureCallbackResource signatureCallbackResource);

    /**
     * Retrieve all applications that have the same signature
     *
     * @param signatureId Unique identifier for the signature
     * @return A list of {@link ApplicationDetails}
     */
    List<ApplicationDetails> getAllApplicationDetailsBy(String signatureId);

    /**
     * Retrieves the signature details including the status from the external signature platform
     *
     * @param signatureId The unique identifier for a signature
     * @return {@link PlatformSignatureDetails} The signature details
     */
    PlatformSignatureDetails getSignatureDetailsFromPlatform(String signatureId);

    /**
     * Find all applications that do not have the maximum number of signatures applied to them
     * and belong to a specific signature request
     *
     * @param signatureId the signature id
     * @return A list of applicationNumbers
     */
    List<String> getNonFullySignedApplicationDetailsBySignatureId(final String username, String signatureId);

    /**
     * Deletes all signatures of a given application.
     *
     * @param applicationNumber   the unique identifier of the application
     */
    void deleteByApplicationNumber(final String username, String applicationNumber);

    /**
     * Delete application.
     *
     * @param username          the username
     * @param applicationNumber the applicationNumber
     */
    String deleteApplication(final String username, final String applicationNumber);

    /**
     * Modify application.
     *
     * @param username          the username
     * @param applicationNumber the applicationNumber
     * @return true if the application is modified otherwise false
     */
    String modifyApplication(final String username, final String applicationNumber);

}
