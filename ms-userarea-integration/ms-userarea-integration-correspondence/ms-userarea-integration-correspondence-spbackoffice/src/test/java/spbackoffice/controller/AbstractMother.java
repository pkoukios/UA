/*
 * $Id:: AbstractMother.java 2021/04/14 11:24 dvelegra
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

package spbackoffice.controller;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.time.ZonedDateTime;

public abstract class AbstractMother {
    private static final ZonedDateTime REFERENCE_DATE = ZonedDateTime.now();
    private static EasyRandomParameters defaultParameters = new EasyRandomParameters()
            .dateRange(REFERENCE_DATE.minusYears(EasyRandomParameters.DEFAULT_DATE_RANGE).toLocalDate(),
                    REFERENCE_DATE.minusDays(1L).toLocalDate())
            .objectPoolSize(100)
            .stringLengthRange(4, 10)
            .collectionSizeRange(1, 2);


    public static <T> T random(Class<T> theClass) {
        return new EasyRandom(defaultParameters).nextObject(theClass);
    }
}
