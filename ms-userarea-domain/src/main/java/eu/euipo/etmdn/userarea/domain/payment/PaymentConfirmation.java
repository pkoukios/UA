/*
 * $Id:: PaymentConfirmation.java 2021/05/25 10:30 dvelegra
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

import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmation {

    private LocalDateTime dateTimeOfSubmission;
    private PaymentType paymentMethod;
    private String transactionId;
    private String confirmationId;
    private String paymentReference;
    private String paidBy;
    private PaymentStatus paymentStatus;
    private List<ShoppingCartApplication> applications;
    private int applicationsCount;
    private String total;
}