/*
 * $Id:: SearchCriteriaShoppingCartMapper.java 2021/04/19 12:30 tantonop
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

package eu.euipo.etmdn.userarea.ws.mapper.shoppingcart;

import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchCriteriaShoppingCart;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.SearchCriteriaShoppingCartResource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SearchCriteriaShoppingCartMapper {

    SearchCriteriaShoppingCartMapper MAPPER = Mappers.getMapper(SearchCriteriaShoppingCartMapper.class);

    SearchCriteriaShoppingCartResource map(SearchCriteriaShoppingCart searchCriteriaShoppingCart);
    SearchCriteriaShoppingCart map(SearchCriteriaShoppingCartResource searchCriteriaShoppingCartResource);

}
