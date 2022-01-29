/*
 * $Id:: ShoppingCartMapper.java 2021/04/17 01:46 tantonop
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

package eu.euipo.etmdn.userarea.persistence.mapper.shoppingcart;

import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCart;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ShoppingCartMapper {

    ShoppingCartMapper MAPPER = Mappers.getMapper(ShoppingCartMapper.class);

    ShoppingCartEntity mapToEntity(ShoppingCart shoppingCart);
    ShoppingCart mapToDomain(ShoppingCartEntity shoppingCartEntity);

}
