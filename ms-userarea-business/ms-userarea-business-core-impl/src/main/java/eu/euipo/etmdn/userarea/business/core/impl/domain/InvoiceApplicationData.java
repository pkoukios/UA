/*
 * $Id:: ShoppingCartApplicationSearch.java 2021/04/17 11:07 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The shopping cart application search core domain object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceApplicationData {

    private String type;
    private String number;
    private String applicant;
    private String representative;
    private String fees;

}
