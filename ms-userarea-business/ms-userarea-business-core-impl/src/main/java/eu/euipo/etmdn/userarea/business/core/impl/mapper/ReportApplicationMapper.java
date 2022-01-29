/*
 * $Id:: ReportApplicationMapper.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.mapper;

import eu.euipo.etmdn.userarea.business.core.impl.domain.ApplicationReportDetails;
import eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.LocarnoDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportApplicationMapper {

    ReportApplicationMapper MAPPER = Mappers.getMapper(ReportApplicationMapper.class);

    @Mappings({
        @Mapping(target = "applicationDate", source = "applicationDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "creationDate", source = "creationDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "statusDate", source = "statusDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "registrationDate", source = "registrationDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "expirationDate", source = "expirationDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "publicationDate", source = "publicationDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "lastModifiedDate", source = "lastModifiedDate", qualifiedByName = "dateConverter"),
        @Mapping(target = "deferPublication", source = "deferPublication", qualifiedByName = "booleanToStringConverter"),
        @Mapping(target = "locarnos", source = "locarnos", qualifiedByName = "convertLocarnoClassesToString")
    })
    ApplicationReportDetails toApplicationReportDetails(ApplicationDetails application);

    default String emptyStringToNotApplicable(String s) {
        return ApplicationUtils.getStringValue(s);
    }

    @Named("dateConverter")
    static String dateConverter(LocalDateTime dateTime) {
        return ApplicationUtils.convertDateToString(dateTime);
    }

    @Named("booleanToStringConverter")
    static String booleanToStringConverter(Boolean answer) {
        return ApplicationUtils.convertBooleanToStringAnswer(answer);
    }

    @Named("convertLocarnoClassesToString")
    static String convertLocarnoClassesToString(List<LocarnoDetails> locarnoClasses) {
        return ApplicationUtils.convertLocarnoClassesToString(locarnoClasses);
    }
}
