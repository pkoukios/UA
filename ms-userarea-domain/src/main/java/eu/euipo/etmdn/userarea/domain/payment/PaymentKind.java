/*
 * $Id:: PaymentKind.java 2021/05/25 10:30 dvelegra
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

package eu.euipo.etmdn.userarea.domain.payment;

public enum PaymentKind {
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer"),
    DEBIT_CARD("Debit Card"),
    ONLINE_PAYMENT("Online Payment"),
    POSTAL_ORDER("Postal Order"),
    CURRENT_ACCOUNT("Current Account"),
    OTHER("Other");

    String value;

    PaymentKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
