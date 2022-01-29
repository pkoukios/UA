/*
 * $Id:: SearchCriteriaShoppingCartResource.java 2021/04/19 11:52 tantonop
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

package eu.euipo.etmdn.userarea.ws.domain.shoppingcart;

import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchShoppingCartSort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The search shopping car criteria resource object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaShoppingCartResource {

    private SearchShoppingCartSort sort;
    private String sortType;

}
