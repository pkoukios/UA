/*
 * $Id:: ApplicationControllerIntTest.java 2021/05/06 12:55 achristo
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

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.domain.exception.FrontofficeReceiptNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import javax.jcr.Repository;
import java.util.Arrays;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ApplicationControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";
    private static final String API_RESOURCE_ROOT = "/api/v1/applications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Repository jcrRepository;

//    @MockBean
//    private RestTemplate restTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private Authentication authentication;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoApplication() {

            MemoryNodeStore ns = new MemoryNodeStore();
            Repository repository = new Jcr(new Oak(ns)).createRepository();

            log.info("Repository created");

            return repository;
        }
    }

    @Test
    @WithMockUser(username = "test", roles = {"TRADEMARKS", "DESIGNS"})
    @DisplayName("When request for a receipt return the pdf")
    public void shouldReturnAPdfFile() throws Exception {
        String applicationNumber = "EF123123123123";
        String mockPdfContent = "fdskalfsdlkafjsdklfajasfd";
        byte[] pdfData = mockPdfContent.getBytes();
        FileResponse mockFileResponse = new FileResponse();
        mockFileResponse.setBytes(pdfData);
        mockFileResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        mockFileResponse.setFileName(applicationNumber + "_receipt.pdf");

        when(applicationService.getReceiptFromFrontoffice(applicationNumber, true)).thenReturn(mockFileResponse);

        this.mockMvc.perform(
                get(API_RESOURCE_ROOT + "/" + applicationNumber + "/receipt").accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(content().bytes(pdfData))
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = {"TRADEMARKS", "DESIGNS"})
    @DisplayName("When request for a receipt return the pdf")
    public void shouldReturnNoContentWhenApplicationIsNotFoundInFrontoffice() throws Exception {
        String applicationNumber = "EF1231231231NOTAVAILABLEINFO";
        when(applicationService.getReceiptFromFrontoffice(applicationNumber, true)).thenThrow(new FrontofficeReceiptNotFoundException("Receipt not found in frontoffice"));

        this.mockMvc.perform(
                get(API_RESOURCE_ROOT + "/" + applicationNumber + "/receipt"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @Disabled
    public void shouldGetAnAttachmentFromFrontoffice() {
        log.info(">>> Requesting receipt from frontoffice...");
        FileResponse fileResponse = new FileResponse();
        String applicationNumber = "EFEM202100000000860";
        String frontofficeUrl = "https://euipo345.am.intrasoft-euipo.int:8443";
        String frontofficeReceiptEndpoint = "/application/draftReceipt/";
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate theRestTemplate = new RestTemplate(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_PDF));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = theRestTemplate.exchange(frontofficeUrl + frontofficeReceiptEndpoint + applicationNumber, HttpMethod.GET, entity, byte[].class);
        assertNotNull(response);

    }


}