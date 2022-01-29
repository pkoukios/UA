/*
 * $Id:: BOAttachments.java 2021/10/05 01:34 tantonop
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

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BOAttachments {

    private String link;
    private String type;
    private long size;
    private String name;
    private boolean body;

}
