/*
 * $Id:: FOSignatureResource.java 2021/05/12 07:01 achristo
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

package eu.euipo.etmdn.userarea.domain.signature;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FOSignatureResource {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fullName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String capacity;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String signatureId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    private List<String> applicationNumbers;
}
