/*
 * $Id:: ShoppingCartSearchResource.java 2021/04/19 10:59 tantonop
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * the shopping cart search resource
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartSearchResource {

    List<ShoppingCartApplicationResource> content;

}
