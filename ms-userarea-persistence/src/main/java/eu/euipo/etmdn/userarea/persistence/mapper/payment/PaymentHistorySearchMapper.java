/*
 * $Id:: PaymentHistorySearchMapper.java 2021/03/09 07:18 dvelegra
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intellectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.persistence.mapper.payment;

import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearch;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentHistorySearchMapper {

    PaymentHistorySearchMapper MAPPER = Mappers.getMapper(PaymentHistorySearchMapper.class);

    @Mapping(target = "paymentId", source = "confirmationId")
    @Mapping(target = "paymentDate", source = "submissionDateTime")
    @Mapping(target = "paymentMethod", source = "type")
    @Mapping(target = "totalCost", source = "total")
    PaymentHistorySearch map(PaymentEntity paymentEntity);

}
