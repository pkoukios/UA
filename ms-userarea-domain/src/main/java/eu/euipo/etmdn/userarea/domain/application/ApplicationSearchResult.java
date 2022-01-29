/*
 * $Id:: ApplicationSearchResult.java 2021/08/04 09:07 dvelegra
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The ApplicationSearchResult domain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSearchResult {

    private List<ApplicationDetails> content;
    private int pageNumber;
    private int pageSize;
    private Long totalResults;
    private int totalPages;
    private int numberOfElements;


}
