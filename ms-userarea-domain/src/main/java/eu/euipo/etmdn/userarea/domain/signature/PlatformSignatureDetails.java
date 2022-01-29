/*
 * $Id:: PlatformSignatureDetails.java 2021/05/13 01:46 dvelegra
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

package eu.euipo.etmdn.userarea.domain.signature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSignatureDetails {

    private SignatoryDetails signatoryDetails;
    private String signatureId;
    private String signedAt;
    private String signingId;
    private String status;
    private boolean isValid;
    private String errorCode;
    private String errorMessage;
}