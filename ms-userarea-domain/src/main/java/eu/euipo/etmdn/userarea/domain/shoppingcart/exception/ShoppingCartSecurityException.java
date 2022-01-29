/*
 * $Id:: ShoppingCartSecurityException.java 2021/04/21 04:21 tantonop
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

package eu.euipo.etmdn.userarea.domain.shoppingcart.exception;

/**
 * exception that is thrown when a user tries to modify an application that does not belong to his shopping cart
 */
public class ShoppingCartSecurityException extends RuntimeException {

    public ShoppingCartSecurityException(String message){
        super(message);
    }

}
