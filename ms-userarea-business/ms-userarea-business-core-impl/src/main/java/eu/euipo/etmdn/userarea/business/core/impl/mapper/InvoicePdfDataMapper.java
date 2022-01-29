/*
 * $Id:: InvoicePdfDataMapper.java 2021/04/02 04:12 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intellectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.business.core.impl.mapper;

import eu.euipo.etmdn.userarea.business.core.impl.domain.InvoiceApplicationData;
import eu.euipo.etmdn.userarea.business.core.impl.domain.InvoicePdfData;
import eu.euipo.etmdn.userarea.domain.payment.InvoiceDetails;
import eu.euipo.etmdn.userarea.domain.payment.PaidApplication;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmation;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.EURO_SYMBOL;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvoicePdfDataMapper {

    InvoicePdfDataMapper MAPPER = Mappers.getMapper(InvoicePdfDataMapper.class);

    @Mappings({
            @Mapping(target = "paymentDate", source = "dateTimeOfSubmission", qualifiedByName = "dateConverter"),
            @Mapping(target = "paymentTime", source = "dateTimeOfSubmission", qualifiedByName = "timeConverter"),
            @Mapping(target = "numberOfApplications", source = "applicationsCount", qualifiedByName = "stringConverter"),
            @Mapping(target = "paymentId", source = "confirmationId")
    })
    InvoicePdfData map(InvoiceDetails invoiceDetails);

    @AfterMapping
    default void setFees(@MappingTarget InvoiceApplicationData.InvoiceApplicationDataBuilder invoiceApplicationData, PaidApplication application) {
        invoiceApplicationData.fees(application.getFees() != null ?
                application.getFees().concat(StringUtils.SPACE).concat(EURO_SYMBOL) :
                StringUtils.EMPTY);
    }

    @AfterMapping
    default void setPaymentMethod(@MappingTarget InvoicePdfData.InvoicePdfDataBuilder invoicePdfData, PaymentConfirmation paymentConfirmation) {
        invoicePdfData.paymentMethod(paymentConfirmation.getPaymentMethod().getValue());
    }

    @AfterMapping
    default void setTotal(@MappingTarget InvoicePdfData.InvoicePdfDataBuilder invoicePdfData, PaymentConfirmation paymentConfirmation) {
        invoicePdfData.total(paymentConfirmation.getTotal().concat(StringUtils.SPACE).concat(EURO_SYMBOL));
    }


    @Named("stringConverter")
    static String stringConverter(int value) {
        return String.valueOf(value);
    }

    @Named("dateConverter")
    static String dateConverter(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

    @Named("timeConverter")
    static String timeConverter(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return dateTime.format(formatter);
    }

}
