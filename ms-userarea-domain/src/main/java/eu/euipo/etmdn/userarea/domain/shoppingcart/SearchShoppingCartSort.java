/*
 * $Id:: SearchShoppingCartSort.java 2021/04/19 11:20 tantonop
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

/**
 * enum for the shopping cart search
 */
public enum SearchShoppingCartSort {

    TYPE("foModule"),
    APPLICATION("number"),
    APPLICANTS("applicant"),
    REPRESENTATIVES("representative"),
    LAST_MODIFIED_DATE("lastModifiedDate"),
    LAST_MODIFIED_BY("lastModifiedBy"),
    FEES("fees");

    private String value;

    SearchShoppingCartSort(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }


}
