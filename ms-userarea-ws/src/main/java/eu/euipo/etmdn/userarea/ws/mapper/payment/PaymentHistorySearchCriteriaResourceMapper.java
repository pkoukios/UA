/*
 * $Id:: PaymentHistorySearchCriteriaResourceMapper.java 2021/03/09 08:15 dvelegra
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intellectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.mapper.payment;

import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchCriteria;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentHistorySearchCriteriaResource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentHistorySearchCriteriaResourceMapper {

    PaymentHistorySearchCriteriaResourceMapper MAPPER = Mappers.getMapper(PaymentHistorySearchCriteriaResourceMapper.class);

    PaymentHistorySearchCriteria map(PaymentHistorySearchCriteriaResource paymentHistorySearchCriteriaResource);
    PaymentHistorySearchCriteriaResource map(PaymentHistorySearchCriteria paymentHistorySearchCriteria);

}
