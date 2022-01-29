/*
 * $Id:: ApplicationRequest.java 2021/08/03 05:09 dvelegra
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

package eu.euipo.etmdn.userarea.domain.application;

import eu.euipo.etmdn.userarea.common.domain.FilteringData;
import eu.euipo.etmdn.userarea.common.domain.PaginationData;
import eu.euipo.etmdn.userarea.common.domain.SortingData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    private String userName;
    private String applicationType;
    private Boolean isDraft;
    private List<String> columns;
    private PaginationData paginationData;
    private SortingData sortingData;
    private FilteringData filteringData;
    private String searchingData;

}
