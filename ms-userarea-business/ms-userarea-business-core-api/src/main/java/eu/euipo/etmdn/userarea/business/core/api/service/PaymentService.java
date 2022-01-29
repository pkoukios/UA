/*
 * $Id:: PaymentService.java 2021/04/27 12:48 achristo
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

import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentResult;
import eu.euipo.etmdn.userarea.domain.payment.InvoiceDetails;
import eu.euipo.etmdn.userarea.domain.payment.PaymentCallbackResource;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmation;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchCriteria;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatusResult;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentApplicationEntity;
import java.util.List;

public interface PaymentService {

    /**
     * Retrieve payment details about a payment transaction.
     *
     * @param transactionId the unique payment transaction id provided by the external platform
     * @return a payment confirmation response
     */
    PaymentConfirmation getConfirmation(String transactionId);

    /**
     * Retrieve invoice data about a payment transaction.
     *
     * @param transactionId the unique payment transaction id provided by the external platform
     * @return a {@link InvoiceDetails} payment invoice details
     */
    InvoiceDetails getInvoice(String transactionId);

    /**
     * Initializes a new payment request to the external payment platform and retrieves
     * a unique transaction id.
     * This method will setup the payment information so that the user when is redirected
     * to pay the platform know the details such as the shopping cart identifier and the total amount.
     *
     * @param initiatePaymentDetails the payment details to be sent to the external payment platform
     * @return a Payment response with unique identifier for the transaction. It will be used to form the redirection url
     */
    InitiatePaymentResult initiatePayment(String username, InitiatePaymentDetails initiatePaymentDetails);

    /**
     * Check the status of a payment in the local database. The payment status is updated from the
     * callback of the payment platform and the results are written in userarea database.
     *
     * @param transactionId the unique identifier of a payment shared between the payment platform and userarea
     * @return a {@link PaymentStatusResult} with details about the payment
     */
    PaymentStatusResult checkStatus(String transactionId);

    /**
     * Saves to userarea db the status of a transaction. Called from the payment platform to notify userarea
     *
     * @param paymentCallbackResource details about the payment transaction
     */
    void confirm(PaymentCallbackResource paymentCallbackResource);

    /**
     * Retrieves the payment history of a user.
     *
     * @param username the username
     * @param paymentHistorySearchCriteria the payment history search criteria
     * @return {@link PaymentHistorySearchResult} the payment history results
     */
    PaymentHistorySearchResult getPaymentHistory(final String username, PaymentHistorySearchCriteria paymentHistorySearchCriteria);

    /**
     * Retrieves the payment application details by application id.
     *
     * @param id the application id
     * @return {@link PaymentApplicationEntity} the payment application entity
     */
    PaymentApplicationEntity getPaymentApplicationByApplicationId(Long id);

    /**
     * Retrieves the payment application details by application id.
     *
     * @param applicationNumber the application number
     * @return {@link PaymentApplicationEntity} the payment application entity
     */
    List<PaymentApplicationEntity> getPaymentApplicationByApplicationNumber(String applicationNumber);
}
