/*
 * $Id:: CorrespondencePdfDataMapper.java 2021/04/02 04:12 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.mapper;

import eu.euipo.etmdn.userarea.business.core.impl.domain.CorrespondencePdfData;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.DraftEntity;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.MessageEntity;
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

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CorrespondencePdfDataMapper {

    CorrespondencePdfDataMapper MAPPER = Mappers.getMapper(CorrespondencePdfDataMapper.class);

    @Mappings({
            @Mapping(target = "sentReceivedDate", source = "receivedDate", qualifiedByName = "dateConverter"),
            @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "dateConverter"),
    })
    CorrespondencePdfData map(MessageEntity messageEntity);

    @Mappings({
            @Mapping(target = "sentReceivedDate", source = "message.receivedDate"),
            @Mapping(target = "dueDate", source = "message.dueDate", qualifiedByName = "dateConverter"),
            @Mapping(target = "procedure", source = "message.procedure"),
            @Mapping(target = "applicationId", source = "message.applicationId"),
            @Mapping(target = "subject", source = "message.subject"),
    })
    CorrespondencePdfData map(DraftEntity draftEntity);

    @Named("dateConverter")
    static String dateConverter(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateTime.format(formatter);
    }
    @AfterMapping
    default void doProcess(@MappingTarget CorrespondencePdfData.CorrespondencePdfDataBuilder correspondencePdfData, DraftEntity entity)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if(MessageStatus.SENT.getValue().equalsIgnoreCase(entity.getDraftStatus())){
            correspondencePdfData.sentReceivedDate(entity.getActionDate().format(formatter));
        }else{
            correspondencePdfData.sentReceivedDate("");
        }
    }

}
