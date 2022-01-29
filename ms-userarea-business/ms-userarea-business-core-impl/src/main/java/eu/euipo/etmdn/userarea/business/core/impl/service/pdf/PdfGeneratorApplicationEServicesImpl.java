/*
 * $Id:: PdfGeneratorApplicationEServicesImpl.java 2021/04/02 02:44 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.service.pdf;

import eu.euipo.etmdn.userarea.business.core.api.service.PdfGenerator;
import eu.euipo.etmdn.userarea.business.core.impl.domain.ApplicationReportDetails;
import eu.euipo.etmdn.userarea.business.core.impl.mapper.ReportApplicationMapper;
import eu.euipo.etmdn.userarea.common.domain.PdfTemplateType;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Trademark implementation for the PDF
 */
@Data
@AllArgsConstructor
public class PdfGeneratorApplicationEServicesImpl implements PdfGenerator {

    private static final String FILENAME = "eservices.pdf";
    private static final String DATA_NAME = "applications";
    private ApplicationSearchResult applications;
    private Set<String> roles;
    private boolean isDraft;


    @Override
    public ApplicationSearchResult getApplications() {
        return this.applications;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public Boolean isDraft() {
        return this.isDraft;
    }

    @Override
    public PdfTemplateType getPdfTemplateType() {
        if(isDraft()){
            return PdfTemplateType.ESERVICE_DRAFT;
        }
        return PdfTemplateType.ESERVICE;
    }

    @Override
    public String getFileName() {
        return FILENAME;
    }

    @Override
    public Map<String, Object> getData() {
        List<ApplicationReportDetails> reportDetails = getApplications().getContent().stream()
                .map(ReportApplicationMapper.MAPPER::toApplicationReportDetails)
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put(DATA_NAME, reportDetails);
        return map;
    }
}
