/*
 * $Id:: ApplicationReportDetails.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationReportDetails {

    private String number;
    private String applicationDate;
    private String creationDate;
    private String lastModifiedDate;
    private String lastModifiedBy;
    private String registrationNumber;
    private String registrationDate;
    private String status;
    private String statusDate;
    private String publicationDate;
    private String expirationDate;
    private String type;
    private String kind;
    private String denomination;
    private String niceClass;
    private String designNumber;
    private String indication;
    private String locarnos;
    private String deferPublication;
    private String associatedDesignNumber;
    private String eserviceName;
    private String associatedRight;
    private String applicant;
    private String representative;
    private String designer;
    private String note;
    private String graphicalRepresentation;

}