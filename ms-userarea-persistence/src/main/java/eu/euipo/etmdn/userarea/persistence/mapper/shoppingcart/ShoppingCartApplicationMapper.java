/*
 * $Id:: ShoppingCartApplicationMapper.java 2021/04/17 02:36 tantonop
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

import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.DESIGN;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TRADEMARK;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.UA_DS_EFILING;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.UA_TM_EFILING;

@Mapper
public interface ShoppingCartApplicationMapper {

    ShoppingCartApplicationMapper MAPPER = Mappers.getMapper(ShoppingCartApplicationMapper.class);

    @Mapping(target = "foModule", source = "type")
    ShoppingCartApplicationEntity mapToEntity(ShoppingCartApplication shoppingCartApplication);
    @Mapping(target = "type", source = "foModule")
    ShoppingCartApplication mapToDomain(ShoppingCartApplicationEntity shoppingCartApplicationEntity);

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "type", source = "foModule")
    @Mapping(target = "lastModifiedBy", source = "lastModifiedBy", qualifiedByName = "getLastModifiedByUsername")
    ShoppingCartApplication mapApplicationToShoppingCartApplication(Application application, @Context String lastModifiedUsername);

    @AfterMapping
    default void doProcess(@MappingTarget ShoppingCartApplication.ShoppingCartApplicationBuilder shoppingCartApplication, ShoppingCartApplicationEntity shoppingCartApplicationEntity){
        if(shoppingCartApplicationEntity.getFoModule().equalsIgnoreCase(TRADEMARK)){
            shoppingCartApplication.type(UA_TM_EFILING);
        } else if(shoppingCartApplicationEntity.getFoModule().equalsIgnoreCase(DESIGN)){
            shoppingCartApplication.type(UA_DS_EFILING);
        }
    }

    @Named("getLastModifiedByUsername")
    default String getLastModifiedByUsername(String lastModifiedUsername) {
        return lastModifiedUsername;
    }
}
