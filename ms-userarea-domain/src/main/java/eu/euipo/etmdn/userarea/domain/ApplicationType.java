/*
 * $Id:: ApplicationType.java 2021/02/10 06:06 dvelegra
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

package eu.euipo.etmdn.userarea.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ApplicationType.
 */
public enum ApplicationType {

	TRADEMARK("trademark"),
	DESIGN("design"),
	ESERVICE("eservice");

	public final String value;

	ApplicationType(final String value) {
		this.value = value;
	}

    public List<ApplicationType> getApplicationTypes() {
    	ArrayList<ApplicationType> values = new ArrayList<>();
		Collections.addAll(values, ApplicationType.values());
    	return values;
    }

	public static ApplicationType getApplicationType(String text) {
		return Arrays.stream(values())
				.filter(type -> type.value.equalsIgnoreCase(text))
				.findFirst().orElse(null);
	}

}

