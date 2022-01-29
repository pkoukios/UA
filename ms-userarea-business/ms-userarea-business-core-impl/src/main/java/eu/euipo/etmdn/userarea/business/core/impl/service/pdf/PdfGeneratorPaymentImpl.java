/*
 * $Id:: PdfGeneratorApplicationCorrespondenceImpl.java 2021/04/02 02:26 tantonop
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
import eu.euipo.etmdn.userarea.business.core.impl.domain.InvoicePdfData;
import eu.euipo.etmdn.userarea.common.domain.PdfTemplateType;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Correspondence implementation for the PDF
 */
@Getter
@Setter
@AllArgsConstructor
public class PdfGeneratorPaymentImpl implements PdfGenerator {

    private static final String FILENAME = "invoice.pdf";

    private InvoicePdfData data;

    @Override
    public ApplicationSearchResult getApplications() {
        return null;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public Boolean isDraft() {
        return null;
    }

    @Override
    public PdfTemplateType getPdfTemplateType() {
        return PdfTemplateType.INVOICE;
    }

    @Override
    public String getFileName() {
        return FILENAME;
    }

    @Override
    public Map<String, Object> getData() {
        Map<String, Object> map = new HashMap<>();
        map.put("data", this.data);
        return map;
    }
}
