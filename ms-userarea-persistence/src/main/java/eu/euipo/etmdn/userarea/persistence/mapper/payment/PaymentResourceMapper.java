/*
 * $Id:: PaymentResourceMapper.java 2021/05/18 02:05 achristo
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.persistence.mapper.payment;

import eu.euipo.etmdn.userarea.domain.payment.PaymentStatusResult;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentResourceMapper {

    PaymentResourceMapper MAPPER = Mappers.getMapper(PaymentResourceMapper.class);

    @Mapping(target = "lastUpdated", source = "updatedAt" )
    PaymentStatusResult map(PaymentEntity paymentEntity);

}
