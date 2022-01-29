/*
 * $Id:: MessageBOMapper.java 2021/10/11 03:58 tantonop
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

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice;

import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.BOMessage;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageBOMapper {

    MessageBOMapper MAPPER = Mappers.getMapper(MessageBOMapper.class);

    Message map(BOMessage boMessage);

    BOMessage map(Message message);

}
