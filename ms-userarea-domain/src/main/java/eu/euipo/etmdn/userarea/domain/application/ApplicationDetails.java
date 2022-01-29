/*
 * $Id:: ApplicationDetails.java 2021/08/04 11:15 dvelegra
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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetails {

    private Long id;
    private String number;
    private LocalDateTime applicationDate;
    private LocalDateTime creationDate;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
    private String registrationNumber;
    private LocalDateTime registrationDate;
    private String status;
    private LocalDateTime statusDate;
    private LocalDateTime publicationDate;
    private LocalDateTime expirationDate;
    private String applicant;
    private String representative;
    private String note;
    private String graphicalRepresentation;
    private String type;
    private String kind;
    private String denomination;
    private String niceClass;
    private String designNumber;
    private String indication;
    private List<LocarnoDetails> locarnos;
    private Boolean deferPublication;
    private Integer associatedDesignNumber;
    private String designer;
    private String ipRightType;
    private String eserviceName;
    private String associatedRight;
    private Boolean locked;
    private BigDecimal fees;
    private String uniqueNumber;

}
