/*
 * $Id:: PaymentHistorySearchCriteria.java 2021/06/29 02:25 dvelegra
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

/**
 * The SearchCriteriaResource domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistorySearchCriteria {

    private int requestPage;
    private int size;
    private PaymentHistorySearchSort sort;
    private String sortType;
    private PaymentHistorySearchFilterCriteria filterCriteria;
    private String search;

}
