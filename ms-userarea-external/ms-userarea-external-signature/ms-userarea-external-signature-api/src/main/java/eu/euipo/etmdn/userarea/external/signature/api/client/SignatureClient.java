/*
 * $Id:: SignatureClient.java 2021/04/05 10:32 achristo
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

package eu.euipo.etmdn.userarea.external.signature.api.client;

import eu.euipo.etmdn.userarea.domain.signature.PlatformSignatureDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;

public interface SignatureClient {

    /**
     * Connects to external signature platform and receives a signature id
     *
     * @param signRequest
     * @return String the signature id
     */
    String createSignatureId(SignRequest signRequest);

    PlatformSignatureDetails getSignatureDetails(String signatureId);

}
