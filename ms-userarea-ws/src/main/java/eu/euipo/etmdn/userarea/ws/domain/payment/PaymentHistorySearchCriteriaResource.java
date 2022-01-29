/*
 * $Id:: PaymentHistorySearchCriteriaResource.java 2021/06/30 03:57 dvelegra
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

import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchFilterCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * The PaymentHistorySearchCriteriaResource domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistorySearchCriteriaResource {

    @NonNull
    private int requestPage;
    @NonNull
    private int size;
    @NonNull
    private PaymentHistorySearchSort sort;
    @NonNull
    private String sortType;
    private PaymentHistorySearchFilterCriteria filterCriteria;
    private String search;

}
