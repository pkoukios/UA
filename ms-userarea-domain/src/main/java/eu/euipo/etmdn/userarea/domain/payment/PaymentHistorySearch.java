/*
 * $Id:: PaymentHistorySearch.java 2021/06/29 02:24 dvelegra
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The Draft Sent Message Search Result domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistorySearch {

    private Long id;
    private String transactionId;
    private String applicationNumbers;
    private String paymentId;
    private LocalDateTime paymentDate;
    private String paidBy;
    private String paymentMethod;
    private String totalCost;

}
