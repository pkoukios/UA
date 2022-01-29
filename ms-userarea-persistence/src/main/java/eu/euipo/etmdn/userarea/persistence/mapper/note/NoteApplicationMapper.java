/*
 * $Id:: NoteApplicationMapper.java 2021/08/02 04:49 dvelegra
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

package eu.euipo.etmdn.userarea.persistence.mapper.note;

import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NoteApplicationMapper {

    NoteApplicationMapper MAPPER = Mappers.getMapper(NoteApplicationMapper.class);

    @Mapping(target = "applicationIdentifier", source = "applicationNumber")
    NoteApplication map(NoteApplicationEntity noteApplicationEntity);
    List<NoteApplication> map(List<NoteApplicationEntity> noteApplicationEntity);
    @Mapping(target = "applicationNumber", source = "applicationIdentifier")
    NoteApplicationEntity map(NoteApplication noteApplication);

}
