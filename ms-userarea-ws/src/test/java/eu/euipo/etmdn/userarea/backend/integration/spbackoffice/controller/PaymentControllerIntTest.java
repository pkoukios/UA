/*
 * $Id:: PaymentControllerIntTest.java 2021/05/24 01:57 dvelegra
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
import eu.euipo.etmdn.userarea.business.core.impl.service.payment.PaymentServiceImpl;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.repository.ApplicationRepository;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatus;
import eu.euipo.etmdn.userarea.domain.payment.PaymentType;
import eu.euipo.etmdn.userarea.external.payment.api.client.PaymentClient;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import eu.euipo.etmdn.userarea.persistence.repository.payment.PaymentRepository;
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
import org.springframework.test.web.servlet.ResultActions;

import javax.jcr.Repository;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PaymentControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentServiceImpl paymentService;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private Authentication authentication;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestRepositoryConfiguration {
        @Bean
        public Repository getRepoPayment() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @WithMockUser(username = "abc@xyz.com", roles = "PAYMENTS")
    @DisplayName("When a valid request for payment is made, return a redirect url to platform")
    public void shouldCreateAPaymentIdAndRedirectUrlToPaymentPlatformWhenValidRequest() throws Exception {

        InitiatePaymentDetails paymentRequest = new InitiatePaymentDetails("user reference", "CREDIT_CARD", Arrays.asList("applicationNumber1", "applicationNumber2"), 33.3, "");

        String initiatePaymentRequestJsonStr = objectMapper.writeValueAsString(paymentRequest);

        when(paymentClient.createTransaction(paymentRequest)).thenReturn("testTransactionid");

        when(authentication.getName()).thenReturn("abc@xyz.com");

        this.mockMvc.perform(
                post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(initiatePaymentRequestJsonStr)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "PAYMENTS")
    @DisplayName("When a transaction is successful (PAID) then user can view the confirmation page")
    public void shouldRespondWithTransactionConfirmationDetailsWhenTransactionIsPaid() throws Exception {
        String transactionId = "mockTransactionId";

        PaymentEntity paymentEntity = PaymentEntity.builder().applicationNumbers("1234,5678")
                .confirmationId("mockConfirmationId")
                .paidBy("abc@xyz.com")
                .paymentReference("User payment reference")
                .status(PaymentStatus.PAID)
                .createdAt(LocalDateTime.now())
                .submissionDateTime(LocalDateTime.now())
                .transactionId(transactionId)
                .total(BigDecimal.valueOf(300.3))
                .type(PaymentType.CREDIT_CARD)
                .updatedAt(LocalDateTime.now()).build();

        Application application = Application.builder()
                .foModule("trademark")
                .number("1234")
                .applicant("Test applicant")
                .representative("Test representative")
                .status("Submitted")
                .fees(BigDecimal.valueOf(300.3))
                .deleted(false)
                .build();

        when(paymentRepository.findByTransactionId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(applicationRepository.findByNumber(eq("1234"))).thenReturn(Collections.singletonList(application));
        when(authentication.getName()).thenReturn("abc@xyz.com");

        ResultActions result = this.mockMvc.perform(
                get("/payments/confirmation/" + transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(openApi().isValid(OPENAPI_YAML));

        assertNotNull(result);

    }

}