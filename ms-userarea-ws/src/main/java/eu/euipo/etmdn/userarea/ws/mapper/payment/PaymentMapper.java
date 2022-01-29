/*
 * $Id:: PaymentMapper.java 2021/04/27 01:11 achristo
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

package eu.euipo.etmdn.userarea.ws.mapper.payment;

import eu.euipo.etmdn.userarea.domain.payment.InitiatePayment;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmation;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmationResponse;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatusResult;
import eu.euipo.etmdn.userarea.ws.domain.payment.InitiatePaymentRequest;
import eu.euipo.etmdn.userarea.ws.domain.payment.InitiatePaymentResponse;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentStatusResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

    PaymentMapper MAPPER = Mappers.getMapper(PaymentMapper.class);

    InitiatePayment map(InitiatePaymentResponse initiatePayment);

    InitiatePaymentDetails map(InitiatePaymentRequest initiatePaymentRequest);

    InitiatePaymentResponse map(InitiatePaymentResult initiatePaymentResult);

    PaymentConfirmationResponse map(PaymentConfirmation paymentConfirmation);

    @Mapping(target = "isValid", source = "valid")
    PaymentStatusResponse map(PaymentStatusResult paymentStatusResult);

}
