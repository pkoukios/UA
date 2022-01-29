/*
 * $Id:: ApplicationSearchResultResourceMapper.java 2021/08/04 09:07 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intellectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.mapper.application;

import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import eu.euipo.etmdn.userarea.ws.domain.application.ApplicationRequestResource;
import eu.euipo.etmdn.userarea.ws.domain.application.ApplicationSearchResultResource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ApplicationSearchMapper {

    ApplicationSearchMapper MAPPER = Mappers.getMapper(ApplicationSearchMapper.class);

    ApplicationSearchResultResource map(ApplicationSearchResult applicationSearchResult);
    ApplicationRequest map(ApplicationRequestResource applicationRequestResource);

}
