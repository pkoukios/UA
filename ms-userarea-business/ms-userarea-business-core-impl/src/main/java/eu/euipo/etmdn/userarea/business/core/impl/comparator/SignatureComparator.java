/*
 * $Id:: SignatureComparator.java 2021/05/13 01:46 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.comparator;

import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortColumn;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class SignatureComparator implements Comparator<Signature> {

    private SignatureSortColumn sortColumn;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public SignatureComparator(SignatureSortColumn sortColumn){
        this.sortColumn = sortColumn;
    }


    @Override
    public int compare(Signature signature1, Signature signature2) {
        if(this.sortColumn.equals(SignatureSortColumn.APPLICATION)){
            return signature1.getNumber().compareTo(signature2.getNumber());
        }
        else if(this.sortColumn.equals(SignatureSortColumn.TYPE)){
            return signature1.getType().compareTo(signature2.getType());
        }
        else if(this.sortColumn.equals(SignatureSortColumn.SIGNEDAT)){
            return sortOnDateSigned(signature1,signature2);
        }
        return 0;
    }

    private int sortOnDateSigned(Signature signature1, Signature signature2){
        if (StringUtils.isNotBlank(signature1.getDate()) && StringUtils.isNotBlank(signature2.getDate())) {
            String date1 = signature1.getDate().split(",")[0];
            String date2 = signature2.getDate().split(",")[0];
            LocalDateTime dt1 = LocalDateTime.parse(date1, formatter);
            LocalDateTime dt2 = LocalDateTime.parse(date2, formatter);
            int result = dt1.toLocalDate().compareTo(dt2.toLocalDate()); // Consider only the date portion first.
            result = ((-1) * result);
            if (result == 0) {
                result = dt1.toLocalTime().compareTo(dt2.toLocalTime());
            }
            return result;
        } else if (StringUtils.isBlank(signature1.getDate())) {
            return 1;
        } else if (StringUtils.isBlank(signature2.getDate())) {
            return -1;
        }
        return 0;
    }

}
