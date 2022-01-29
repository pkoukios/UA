/*
 * $Id:: PaymentHistoryDataResource.java 2021/06/30 03:57 dvelegra
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

package eu.euipo.etmdn.userarea.ws.domain.payment;

import eu.euipo.etmdn.userarea.common.domain.KeyValueLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryDataResource {

    private List<KeyValueLabel> ipRightTypes;
    private List<KeyValueLabel> categories;
    private List<KeyValueLabel> dates;

}
