/*
 * $Id:: PdfGeneratorFactory.java 2021/04/02 01:39 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.factory;

import eu.euipo.etmdn.userarea.business.core.api.service.PdfGenerator;
import eu.euipo.etmdn.userarea.business.core.impl.domain.CorrespondencePdfData;
import eu.euipo.etmdn.userarea.business.core.impl.domain.InvoicePdfData;
import eu.euipo.etmdn.userarea.business.core.impl.service.pdf.PdfGeneratorApplicationCorrespondenceImpl;
import eu.euipo.etmdn.userarea.business.core.impl.service.pdf.PdfGeneratorApplicationDesignsImpl;
import eu.euipo.etmdn.userarea.business.core.impl.service.pdf.PdfGeneratorApplicationEServicesImpl;
import eu.euipo.etmdn.userarea.business.core.impl.service.pdf.PdfGeneratorApplicationTrademarksImpl;
import eu.euipo.etmdn.userarea.business.core.impl.service.pdf.PdfGeneratorPaymentImpl;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;

import java.util.Set;

/**
 * factory class for the PDF generator
 */
public class PdfGeneratorFactory {

    /**
     * create a PdfGenerator implementation class
     * @param applicationType the application type
     * @param applications the applications
     * @param roles the roles
     * @param isDraft is draft application
     * @return the pdf generator
     */
    public static PdfGenerator getPdfDataGenerator(ApplicationType applicationType, ApplicationSearchResult applications, Set<String> roles, boolean isDraft){
        switch (applicationType) {
            case TRADEMARK:
                return new PdfGeneratorApplicationTrademarksImpl(applications,roles,isDraft);
            case DESIGN:
                return new PdfGeneratorApplicationDesignsImpl(applications,roles,isDraft);
            case ESERVICE:
                return new PdfGeneratorApplicationEServicesImpl(applications,roles,isDraft);
            default:
                return null;
        }

    }

    /**
     * returns a pdfGenerator for correspondence
     * @param ipoConfiguration the ipo configuration
     * @param domainAccount the domain account
     * @param data the pdf data to be generated
     * @param isDraft if it is a draft correspondence
     * @return the pdf generator
     */
    public static PdfGeneratorApplicationCorrespondenceImpl getPdfDataGenerator(IpoConfiguration ipoConfiguration, DomainAccount domainAccount, CorrespondencePdfData data, boolean isDraft){
        return new PdfGeneratorApplicationCorrespondenceImpl(ipoConfiguration,domainAccount, data, isDraft);
    }

    /**
     * Returns a pdfGenerator for payment invoice.
     *
     * @param data the pdf data to be generated
     * @return the pdf generator
     */
    public static PdfGenerator getPdfDataGenerator(InvoicePdfData data){
        return new PdfGeneratorPaymentImpl(data);
    }

}
