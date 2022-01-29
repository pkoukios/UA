/*
 * $Id:: InitiatePaymentResponse.java 2021/05/25 10:30 dvelegra
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

package eu.euipo.etmdn.userarea.ws.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Class for Initiating the payment procedure
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentResponse {

    private String transactionId;
    private String redirectToPaymentPlatformUrl;

}
