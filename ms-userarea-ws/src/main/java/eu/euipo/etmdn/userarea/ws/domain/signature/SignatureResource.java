/*
 * $Id:: SignatureResource.java 2021/03/12 01:33 tantonop
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intelectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.domain.signature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Signature resource
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResource {

    private String type;
    private String number;
    private String name;
    private String capacity;
    private String date;
}
