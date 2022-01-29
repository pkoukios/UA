/*
 * $Id:: ApplicationUtilsTest.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.utils;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.convertBooleanToStringAnswer;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.convertDateToString;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.convertLocarnoClassesToString;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.getStatusFromFo;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.getStringValue;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.hasDesignRole;
import static eu.euipo.etmdn.userarea.business.core.impl.utils.ApplicationUtils.hasTrademarkRole;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationUtilsTest {

    @Test
    public void testStatusFromFo() {
        final String status = "STATUS_SUBMITTED";
        final String expectedStatus = "Submitted";
        String actualStatus = getStatusFromFo(status);
        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void testTrademarkRole() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_TRADEMARKS");
        roles.add("ROLE_DESIGNS");
        boolean result = hasTrademarkRole(roles);
        assertTrue(result);
    }

    @Test
    public void testDesignRole() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_TRADEMARKS");
        roles.add("ROLE_DESIGNS");
        boolean result = hasDesignRole(roles);
        assertTrue(result);
    }

    /*
    @Test
    public void testConvertLocarnoClassesToString() {
        final String expectedResult = "05.08, 08.05";
        Locarno one = Locarno.builder().mainClass("05").subClass("08").build();
        Locarno two = Locarno.builder().mainClass("08").subClass("05").build();
        List<LocarnoDetails> locarnos = new ArrayList<>();
        locarnos.add(one);
        locarnos.add(two);
        String actualResult = convertLocarnoClassesToString(locarnos);
        assertEquals(expectedResult, actualResult);
    }*/

    @Test
    public void testConvertLocarnoClassesToStringNotApplicable() {
        final String expected = "N/A";
        String actualResult = convertLocarnoClassesToString(null);
        assertEquals(expected, actualResult);
    }

    @Test
    public void testConvertBooleanToStringAnswerYes() {
        final String expected = "Yes";
        String result = convertBooleanToStringAnswer(Boolean.TRUE);
        assertEquals(expected, result);
    }

    @Test
    public void testConvertBooleanToStringAnswerNo() {
        final String expected = "No";
        String result = convertBooleanToStringAnswer(Boolean.FALSE);
        assertEquals(expected, result);
    }


    @Test
    public void testStringValue() {
        final String expected = "aaabbbbccccc";
        String result = getStringValue("aaabbbbccccc");
        assertEquals(expected, result);
    }

    @Test
    public void testStringValueNumber() {
        final String expected = "12345";
        String result = getStringValue("12345");
        assertEquals(expected, result);
    }

    @Test
    public void testStringValueNull() {
        final String expected = "N/A";
        String result = getStringValue(null);
        assertEquals(expected, result);
    }

    @Test
    public void testConvertDateStringValue() {
        final String exprectedDate = "2020-12-21";
        final String str = "2020-12-21 12:21";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        final LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

        String result = convertDateToString(dateTime);
        assertEquals(exprectedDate, result);
    }

    @Test
    public void testConvertNullDateStringValue() {
        final String expected = "N/A";
        String result = convertDateToString(null);
        assertEquals(expected, result);
    }
}