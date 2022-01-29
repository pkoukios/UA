/*
 * $Id:: WebConfiguration.java 2021/04/14 11:24 dvelegra
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

//@formatter:off
/**
 *
 *       . * .
 *     * RRRR  *    Copyright (c) 2020 EUIPO:
 *   .   RR  R   .  European Union Intellectual Property Office
 *   *   RRR     *
 *    .  RR RR  .   ALL RIGHTS RESERVED
 *     * . _ . *
 *
 *   Author: EUIPO
 *
 */
//@formatter:on

package eu.euipo.etmdn.userarea.ws.webmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    // Required to server swagger-ui resources
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/3.43.0/");
    }

    // Required to handle X-Forwarded-* headers in Spring HateOAS
    @Bean
    public Filter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
