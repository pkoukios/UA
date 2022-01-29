/*
 * $Id:: PaymentHistorySearchResultResource.java 2021/06/30 03:57 dvelegra
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

import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The PaymentHistorySearchResultResource domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistorySearchResultResource {

    private List<PaymentHistorySearch> content;
    private int pageNumber;
    private int pageSize;
    private Long totalResults;
    private int totalPages;


}
