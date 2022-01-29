/*
 * $Id:: PdfGenerator.java 2021/04/02 01:31 tantonop
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

package eu.euipo.etmdn.userarea.business.core.api.service;

import eu.euipo.etmdn.userarea.common.domain.PdfTemplateType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;

import java.util.Map;
import java.util.Set;

public interface PdfGenerator {

    ApplicationSearchResult getApplications();

    Set<String> getRoles();

    Boolean isDraft();

    PdfTemplateType getPdfTemplateType();

    String getFileName();

    Map<String, Object> getData();

}
