/*
 * $Id:: OpenApiEndpoint.java 2021/04/14 11:24 dvelegra
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

package eu.euipo.etmdn.userarea.ws.actuator.controller;

import io.micrometer.core.instrument.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

@RestController
@RequestMapping("/actuator/openapi")
public class OpenApiEndpoint {

    public static final String OPENAPI_YAML = "/openapi/openapi.yaml";

    @GetMapping()
    @ResponseBody
    public String openapi() throws Exception {
        final URL resource = this.getClass().getResource(OPENAPI_YAML);
        if (resource == null) {
            return StringUtils.EMPTY;
        }
        return IOUtils.toString(resource.openStream());
    }

}
