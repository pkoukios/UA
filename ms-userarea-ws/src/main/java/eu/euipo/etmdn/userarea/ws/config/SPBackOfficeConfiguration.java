/*
 * $Id:: SPBackOfficeConfiguration.java 2021/05/13 01:46 dvelegra
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

package eu.euipo.etmdn.userarea.ws.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SPBackOfficeConfiguration {

    @Value("${userarea.correspondence.reply.backoffice.service}")
    private String serviceBackOffice;
    @Value("${userarea.correspondence.reply.backoffice.receipt}")
    private String receiptBackOffice;
    @Value("${userarea.correspondence.reply.backoffice.draft}")
    private String draftBackOffice;

}
