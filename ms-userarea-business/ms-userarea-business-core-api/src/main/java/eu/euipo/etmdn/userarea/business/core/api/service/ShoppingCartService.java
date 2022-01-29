/*
 * $Id:: ShoppinCartService.java 2021/04/17 10:38 tantonop
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

package eu.euipo.etmdn.userarea.business.core.api.service;

import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.MainAccount;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchCriteriaShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;

import java.util.List;
import java.util.Set;

/**
 * ShoppingCart Interface for internal handling of shopping carts
 */
public interface ShoppingCartService {

     ShoppingCart create(String username);

     ShoppingCart getByUser(String username);

     ShoppingCart getById(Long id);

     void checkAndAddApplicationToShoppingCart(MainAccount mainAccount, Application application, String lastModifiedBy);

     ShoppingCartSearch getApplications(String username, SearchCriteriaShoppingCart searchCriteriaShoppingCart, Set<String> roles);

     String modifyApplication(String username, String applicationId, boolean isApplicationDeleted, boolean isSignatureDeleted);

     void removeApplication(Long applicationId);

     List<ShoppingCartApplication> getShoppingCartApplicationsByIds(List<Long> ids);

     List<ShoppingCartApplication> getShoppingCartApplicationsByNumbers(List<String> numbers);

}
