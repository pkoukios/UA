/*
 * $Id:: ShoppingCart.java 2021/04/17 10:40 tantonop
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * ShoppingCart core domain object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {

    private Long id;
    private List<ShoppingCartApplication> applications;
    private String user;
    private LocalDateTime createdDate;
}
