/*
 * $Id:: ShoppingCartUtils.java 2021/04/19 12:56 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.utils;

import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.specification.shoppingcart.ShoppingCartApplicationSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public class ShoppingCartUtils {

        public static Specification<ShoppingCartApplicationEntity> getShoppingCartSpecification(Long cartId,Set<String> roles){
                return Specification.where(ShoppingCartApplicationSpecification.filterByCartId(cartId))
                        .and(ShoppingCartApplicationSpecification.filterByIsTrademark(getTypesFromRoles(roles,ApplicationType.TRADEMARK))
                        .or(ShoppingCartApplicationSpecification.filterByIsDesign(getTypesFromRoles(roles,ApplicationType.DESIGN))));
        }



        /**
         * Returns types based on the roles.
         *
         * @param roles the set of roles assigned to account
         * @return a list of message types
         */
        public static boolean getTypesFromRoles(Set<String> roles, ApplicationType applicationType) {
                if(applicationType.value.equalsIgnoreCase(ApplicationType.TRADEMARK.value)){
                        return roles.stream().anyMatch(role -> role.equals("ROLE_TRADEMARKS"));
                }else if(applicationType.value.equalsIgnoreCase(ApplicationType.DESIGN.value)){
                        return roles.stream().anyMatch(role -> role.equals("ROLE_DESIGNS"));
                }
                return  false;
        }

}
