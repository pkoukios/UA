/*
 * $Id:: PaymentHistoryControllerTest.java 2021/04/16 10:33 dvelegra
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

package eu.euipo.etmdn.userarea.ws.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearch;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchCriteria;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchResult;
import eu.euipo.etmdn.userarea.ws.controller.payment.PaymentHistoryController;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentHistorySearchCriteriaResource;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentHistorySearchResultResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class PaymentHistoryControllerTest {

    @InjectMocks
    PaymentHistoryController paymentHistoryController;

    @Mock
    private PaymentService paymentService;
    @Mock
    private PdfService pdfService;

    private Authentication authentication;
    private static final String USERNAME = "abc@xyz.com";
    private static final String TRANSACTION_ID = "12345";

    @Before
    public void setUp() {
        paymentHistoryController = new PaymentHistoryController(paymentService, pdfService);
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(USERNAME);
    }

    @Test
    public void testSearchAccountDetails() {
        PaymentHistorySearch paymentHistorySearch = new PaymentHistorySearch();
        paymentHistorySearch.setPaymentId("12345");
        paymentHistorySearch.setApplicationNumbers("EM20210001,EM20210002,EM20210003");
        PaymentHistorySearchResult paymentHistorySearchResult = new PaymentHistorySearchResult();
        paymentHistorySearchResult.setContent(Collections.singletonList(paymentHistorySearch));
        PaymentHistorySearchCriteriaResource paymentHistorySearchCriteriaResource = new PaymentHistorySearchCriteriaResource();
        paymentHistorySearchCriteriaResource.setRequestPage(0);
        paymentHistorySearchCriteriaResource.setSize(100);
        paymentHistorySearchCriteriaResource.setSortType("DESC");
        PaymentHistorySearchCriteria paymentHistorySearchCriteria = new PaymentHistorySearchCriteria();
        paymentHistorySearchCriteria.setRequestPage(0);
        paymentHistorySearchCriteria.setSize(100);
        paymentHistorySearchCriteria.setSortType("DESC");
        when(paymentService.getPaymentHistory(authentication.getName(), paymentHistorySearchCriteria)).thenReturn(paymentHistorySearchResult);
        ResponseEntity<PaymentHistorySearchResultResource> result = paymentHistoryController.searchPaymentHistory(authentication, paymentHistorySearchCriteriaResource);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        PaymentHistorySearchResultResource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetInvoice() {
        final String fileName = "invoice.pdf";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(pdfService.generatePdf(authentication.getName(), TRANSACTION_ID)).thenReturn(fileInfo);
        ResponseEntity<byte[]> result = paymentHistoryController.downloadInvoiceFromHistory(authentication, TRANSACTION_ID);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        byte[] response = result.getBody();
        assertNotNull(response);
    }

}