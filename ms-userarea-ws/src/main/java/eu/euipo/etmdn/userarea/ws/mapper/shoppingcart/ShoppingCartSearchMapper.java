/*
 * $Id:: ShoppingCartSearchMapper.java 2021/04/19 11:10 tantonop
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

import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.ShoppingCartSearchResource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ShoppingCartSearchMapper {

    ShoppingCartSearchMapper MAPPER = Mappers.getMapper(ShoppingCartSearchMapper.class);

    ShoppingCartSearch map(ShoppingCartSearchResource shoppingCartSearchResource);
    ShoppingCartSearchResource map(ShoppingCartSearch shoppingCartSearch);
}
