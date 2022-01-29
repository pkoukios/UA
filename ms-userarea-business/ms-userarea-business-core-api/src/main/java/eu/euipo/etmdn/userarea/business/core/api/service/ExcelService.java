/*
 * $Id:: ExcelService.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.api.service;

import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;

import java.util.Set;

/**
 * ExcelService Interface for generating excel reports
 */
public interface ExcelService {

    /**
     * Generate excel report for trademarks.
     *
     * @param applicationRequest the application request
     * @param roles the logged in user's set of roles
     * @return {@link FileInfo} the generated excel file reports with trademarks
     */
    FileInfo generateExcel(final ApplicationRequest applicationRequest, Set<String> roles);

}
