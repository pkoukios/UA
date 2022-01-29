/*
 * $Id:: ShoppingCartSearch.java 2021/04/19 11:06 tantonop
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

import java.util.List;

/**
 * the shopping cart search domain object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartSearch {

    List<ShoppingCartApplication> content;

}
