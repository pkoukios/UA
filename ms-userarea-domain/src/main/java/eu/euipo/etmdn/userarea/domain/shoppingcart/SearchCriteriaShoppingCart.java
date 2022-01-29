/*
 * $Id:: SearchCriteriaShoppingCart.java 2021/04/19 11:17 tantonop
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

package eu.euipo.etmdn.userarea.domain.shoppingcart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The serach criteria for the shopping cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaShoppingCart {

    private SearchShoppingCartSort sort;
    private String sortType;


}
