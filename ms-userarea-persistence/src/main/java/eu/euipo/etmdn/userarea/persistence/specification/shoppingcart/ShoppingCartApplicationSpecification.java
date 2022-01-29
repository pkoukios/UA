/*
 * $Id:: ShoppingCartApplicationSpecification.java 2021/04/19 01:24 tantonop
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

package eu.euipo.etmdn.userarea.persistence.specification.shoppingcart;

import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity_;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartEntity_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;

/**
 * Specification class for the shopping cart applications
 */
public class ShoppingCartApplicationSpecification {

    public static Specification<ShoppingCartApplicationEntity> filterByCartId(final Long cartId){
        return (root, query, cb) -> {
            final Path<ShoppingCartApplicationEntity> shoppingCartApplicationCart = root.join(ShoppingCartApplicationEntity_.SHOPPING_CART).get(ShoppingCartEntity_.ID);
            query.groupBy(root.get(ShoppingCartApplicationEntity_.NUMBER));
            return shoppingCartApplicationCart.in(cartId);
        };
    }

    public static Specification<ShoppingCartApplicationEntity> filterByIsTrademark(boolean hasTrademarkRole){
        return (root, query, cb) ->  {
            if (hasTrademarkRole) {
                return cb.isTrue(root.get(ShoppingCartApplicationEntity_.IS_TRADEMARK));
            } else {
                return cb.isFalse(root.get(ShoppingCartApplicationEntity_.IS_TRADEMARK));
            }};
    }

    public static Specification<ShoppingCartApplicationEntity> filterByIsDesign(boolean hasDesignRole){
        return (root, query, cb) ->   {
            if (hasDesignRole) {
                return cb.isTrue(root.get(ShoppingCartApplicationEntity_.IS_DESIGN));
            } else {
                return cb.isFalse(root.get(ShoppingCartApplicationEntity_.IS_DESIGN));
            }};
    }
}
