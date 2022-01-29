/*
 * $Id:: PaymentClient.java 2021/05/11 02:06 achristo
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

package eu.euipo.etmdn.userarea.external.payment.api.client;

import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;

/**
 * Payment Client interface.
 */
public interface PaymentClient {

    /**
     * Connect to external payment platform and create a transaction id
     */
    public String createTransaction(InitiatePaymentDetails initiatePaymentDetails);
}


