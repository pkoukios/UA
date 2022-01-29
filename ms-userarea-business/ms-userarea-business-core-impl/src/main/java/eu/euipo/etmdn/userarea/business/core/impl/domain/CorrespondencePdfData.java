/*
 * $Id:: CorrespondencePdfData.java 2021/04/02 12:48 tantonop
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

package eu.euipo.etmdn.userarea.business.core.impl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * class to represent the correspondence pdf data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrespondencePdfData {

    private String procedure;
    private String applicationId;
    private String sentReceivedDate;
    private String recipientSender;
    private String dueDate;
    private String subject;
    private String body;
    private String ipoDetails;


}
