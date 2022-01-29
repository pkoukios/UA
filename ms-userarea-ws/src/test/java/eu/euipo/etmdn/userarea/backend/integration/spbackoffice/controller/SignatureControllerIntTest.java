/*
 * $Id:: SignatureControllerIntTest.java 2021/05/12 12:03 dvelegra
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
import eu.euipo.etmdn.userarea.business.core.api.service.SignatureService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationResponse;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;
import eu.euipo.etmdn.userarea.domain.signature.SignatoryDetails;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Arrays;
import java.util.HashSet;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SignatureControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignatureService signatureService;

    @MockBean
    private Authentication authentication;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoSignature() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @WithMockUser(username = "test", roles = {"SIGNATURES", "TRADEMARKS"})
    @DisplayName("When an application is deleted from signature")
    public void shouldDeleteApplication() throws Exception {
        when(authentication.getName()).thenReturn("test");
        when(signatureService.deleteApplication(authentication.getName(), "12345")).thenReturn(StringUtils.EMPTY);
        this.mockMvc.perform(
                delete("/signatures/delete/application/trademark/12345")
                        .characterEncoding("UTF-8")
                        .with(csrf()))
                .andExpect(status().isOk()).andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = {"SIGNATURES", "TRADEMARKS"})
    @DisplayName("When an application is modified from signature")
    public void shouldModifyApplication() throws Exception {
        String resumeUrl = "resume";
        ApplicationResponse response = ApplicationResponse.builder().applicationNumber("12345").resumeUrl(resumeUrl).build();
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(response);
        when(authentication.getName()).thenReturn("test");
        when(signatureService.modifyApplication(authentication.getName(), "12345")).thenReturn(resumeUrl);
        this.mockMvc.perform(
                put("/signatures/modify/application/trademark/12345")
                        .characterEncoding("UTF-8")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse)).andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = {"SIGNATURES", "TRADEMARKS"})
    @DisplayName("When an sign request contains wrong email format")
    public void givenWrongSignatoryEmailExpectError() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SignRequest signRequest = SignRequest.builder()
                .applicationIds(new HashSet<>(Arrays.asList("applicationid1", "applicationId2")))
                .signatoryDetails(SignatoryDetails.builder().fullName("Test Testing").email("wrongEmailFormat").capacity("Applicant").build())
                .build();
        String jsonRequest = mapper.writeValueAsString(signRequest);

        this.mockMvc.perform(
                post("/signatures/sign")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding("UTF-8")
                        .with(csrf())
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(status().isBadRequest());
    }

}
