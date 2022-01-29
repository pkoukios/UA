/*
 * $Id:: PaymentHistorySearchSort.java 2021/06/30 04:38 dvelegra
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

/**
 * enum for payment history column sort
 */
public enum PaymentHistorySearchSort {

    PAYMENT_ID("confirmationId"),
    PAID_BY("paidBy"),
    PAYMENT_DATE("submissionDateTime"),
    PAYMENT_METHOD("type"),
    PAYMENT_TOTAL_COST("total");

    private String value;

    PaymentHistorySearchSort(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static PaymentHistorySearchSort fromValue(String text) {
        for (PaymentHistorySearchSort b : PaymentHistorySearchSort.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
