/*
 * $Id:: CorrespondenceDownloadControllerIntTest.java 2021/04/06 04:23 tantonop
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller;

import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.common.business.config.PdfTemplateConfiguration;
import eu.euipo.etmdn.userarea.common.business.helper.PdfTemplateHelper;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.jcr.Repository;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ShoppingCartDownloadControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private PdfService pdfService;
    @MockBean
    private Authentication authentication;
    @MockBean
    private ShoppingCartService shoppingCartService;
    @MockBean
    private AccountService accountService;
    @MockBean
    private PdfTemplateConfiguration pdfTemplateConfiguration;
    @MockBean
    private PdfTemplateHelper pdfTemplateHelper;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoShoppingCartDownload() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @WithMockUser(username = "test", roles = "PAYMENTS")
    @DisplayName("When an invoice download is requested then the invoice is downloaded")
    public void whenMessageDownloadIsRequestedTheMessageIdDownloaded() throws Exception {
        DomainAccount domainAccount = AbstractMother.random(DomainAccount.class);
        when(authentication.getName()).thenReturn("test");
        FileInfo fileResponse = AbstractMother.random(FileInfo.class);
        fileResponse.setFileContent("some content".getBytes());
        fileResponse.setFileName("invoice.pdf");
        when(pdfService.generatePdf("test", "33")).thenReturn(fileResponse);
        when(pdfTemplateHelper.processTemplate(any(), any(), any())).thenReturn("<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "</body>\n" +
                "</html>");
        when(accountService.getMainAccount(any())).thenReturn(domainAccount);
        this.mockMvc.perform(
                get("/shoppingcarts/download/invoice/123").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

}
