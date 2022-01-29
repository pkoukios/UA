/*
 * $Id:: PlatformSignatureDetailsResponse.java 2021/04/13 03:50 achristo
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

package eu.euipo.etmdn.userarea.ws.domain.signature;

import eu.euipo.etmdn.userarea.domain.signature.SignatoryDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSignatureDetailsResponse {

    private String signatureId;
    private String signedAt;
    private String signingId;
    private SignatoryDetails signatoryDetails;
    private String status;
}
