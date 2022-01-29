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
import eu.euipo.etmdn.userarea.business.core.impl.domain.CorrespondencePdfData;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.PdfTemplateType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static eu.euipo.etmdn.userarea.common.business.utils.AccountUtils.getAccountFullName;

/**
 * Correspondence implementation for the PDF
 */
@Getter
@Setter
@AllArgsConstructor
public class PdfGeneratorApplicationCorrespondenceImpl implements PdfGenerator {

    private static final String FILENAME = "correspondence.pdf";

    private IpoConfiguration ipoConfiguration;
    private DomainAccount domainAccount;
    private CorrespondencePdfData data;
    private boolean isDraft;

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
        return this.isDraft;
    }

    @Override
    public PdfTemplateType getPdfTemplateType() {
        if(isDraft()){
            return PdfTemplateType.CORRESPONDENCE;
        }
        return PdfTemplateType.CORRESPONDENCE_DRAFT;
    }

    @Override
    public String getFileName() {
        return FILENAME;
    }

    @Override
    public Map<String, Object> getData() {
        Map<String, Object> map = new HashMap<>();
        String recipientValue = getAccountFullName(domainAccount);
        this.data.setRecipientSender(recipientValue.trim());
        this.data.setBody(this.data.getBody().replaceAll("\\<.*?\\>", ""));
        this.data.setIpoDetails(this.ipoConfiguration.getIpo().get("name")+StringUtils.SPACE+","
                +this.ipoConfiguration.getIpo().get("address")+StringUtils.SPACE+", +"+this.ipoConfiguration.getIpo().get("phone")+
                StringUtils.SPACE+","+this.ipoConfiguration.getIpo().get("email"));
        map.put("data", this.data);
        return map;
    }
}
