/*
 * $Id:: ShoppingCartPdfData.java 2021/04/02 12:48 tantonop
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.business.core.impl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * class to represent the shopping cart pdf data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePdfData {

    private String paymentId;
    private String paymentDate;
    private String paymentTime;
    private String transactionId;
    private String paymentMethod;
    private String paidBy;
    private List<InvoiceApplicationData> applications;
    private String paymentReference;
    private String numberOfApplications;
    private String total;


}
