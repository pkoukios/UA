/*
 * $Id:: ApplicationMapper.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.business.core.impl.domain.InvoiceApplicationData;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.payment.PaidApplication;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TRADEMARK;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.UA_DS_EFILING;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.UA_TM_EFILING;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApplicationMapper {

    ApplicationMapper MAPPER = Mappers.getMapper(ApplicationMapper.class);

    List<ApplicationDetails> toApplicationDetails(final List<Application> applications);
    ApplicationDetails mapToApplicationDetails(final Application application);
    Application toApplication(@MappingTarget Application target, final Application synchApplication);

    List<PaidApplication> toPaidApplications(List<Application> application);

    @AfterMapping
    default void setType(@MappingTarget PaidApplication.PaidApplicationBuilder paidApplication, Application application) {
        if (ApplicationType.ESERVICE.value.equalsIgnoreCase(application.getFoModule())) {
            paidApplication.type(application.getEserviceName());
        } else {
            paidApplication.type(application.getFoModule().equalsIgnoreCase(TRADEMARK) ? UA_TM_EFILING : UA_DS_EFILING);
        }
    }

}
