/*
 * $Id:: SearchCriteriaApplication.java 2021/09/02 05:22 tantonop
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

package eu.euipo.etmdn.userarea.domain.application;

import eu.euipo.etmdn.userarea.common.domain.FilteringData;
import eu.euipo.etmdn.userarea.common.domain.PaginationData;
import eu.euipo.etmdn.userarea.common.domain.SortingData;
import lombok.Data;

import java.util.List;

/**
 * the search criteria
 */
@Data
public class SearchCriteriaApplication {

    private String userName;
    private String applicationType;
    private Boolean isDraft;
    private List<String> columns;
    private PaginationData paginationData;
    private SortingData sortingData;
    private FilteringData filteringData;
    private String searchingData;
    private List<String> roles;
}

