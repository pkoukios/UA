/*
 * $Id:: SignatureResourceMapper.java 2021/03/11 09:07 tantonop
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intelectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.mapper.signature;

import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.ws.domain.signature.SignatureResource;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SignatureResourceMapper {

    SignatureResourceMapper MAPPER = Mappers.getMapper(SignatureResourceMapper.class);

    Signature map(SignatureResource signatureResource);
    SignatureResource map(Signature signature);
    List<Signature> mapSignatures(List<SignatureResource> signatureResources);
    List<SignatureResource> mapSignatureResources(List<Signature> signatures);

}
