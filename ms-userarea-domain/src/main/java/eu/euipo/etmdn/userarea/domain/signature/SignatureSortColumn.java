/*
 * $Id:: SignatureSortColumn.java 2021/05/13 01:46 dvelegra
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

package eu.euipo.etmdn.userarea.domain.signature;

public enum SignatureSortColumn {

    TYPE("TYPE"),
    APPLICATION("APPLICATION"),
    SIGNEDAT("SIGNEDAT");

    private String value;

    SignatureSortColumn(String value) {
        this.value = value;
    }
}
