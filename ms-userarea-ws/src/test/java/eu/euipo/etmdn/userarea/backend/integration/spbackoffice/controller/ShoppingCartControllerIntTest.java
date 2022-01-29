/*
 * $Id:: ShoppingCartControllerIntTest.java 2021/04/26 03:30 dvelegra
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

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchShoppingCartSort;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.SearchCriteriaShoppingCartResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.jcr.Repository;
import java.text.SimpleDateFormat;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ASCENDING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ShoppingCartControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private Authentication authentication;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoShoppingCart() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @WithMockUser(username = "abc@xyz.com", roles = "PAYMENTS")
    @DisplayName("When a request to get the shopping cart applications, the applications should be returned")
    public void shouldRetrieveTheShoppingCartApplications() throws Exception {
        SearchCriteriaShoppingCartResource searchCriteriaShoppingCartResource = new SearchCriteriaShoppingCartResource();
        searchCriteriaShoppingCartResource.setSortType(ASCENDING);
        searchCriteriaShoppingCartResource.setSort(SearchShoppingCartSort.TYPE);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        String jsonStr = mapper.writeValueAsString(searchCriteriaShoppingCartResource);
        ShoppingCartSearch search = AbstractMother.random(ShoppingCartSearch.class);
        when(shoppingCartService.getApplications(any(), any(), any())).thenReturn(search);
        when(authentication.getName()).thenReturn("abc@xyz.com");
        this.mockMvc.perform(
                post("/shoppingcarts")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(jsonStr).
                        with(csrf()))
                .andExpect(status().isOk()).andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "abc@xyz.com", roles = "PAYMENTS")
    @DisplayName("When a request to modify applications, the applications will be modified")
    public void shouldModifyApplicationFromShoppingCart() throws Exception {
        when(authentication.getName()).thenReturn("abc@xyz.com");
        this.mockMvc.perform(
                put("/shoppingcarts/modify/1")
                        .contentType(MediaType.APPLICATION_JSON).
                        with(csrf()))
                .andExpect(status().isOk()).andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "abc@xyz.com", roles = "PAYMENTS")
    @DisplayName("When a request to delete applications, the applications will be deleted")
    public void shouldDeleteApplicationFromShoppingCart() throws Exception {
        when(authentication.getName()).thenReturn("abc@xyz.com");
        this.mockMvc.perform(
                delete("/shoppingcarts/delete/1")
                        .contentType(MediaType.APPLICATION_JSON).
                        with(csrf()))
                .andExpect(status().isOk()).andExpect(openApi().isValid(OPENAPI_YAML));
    }


}
