/*
 * $Id:: CorrespondenceDownloadControllerTest.java 2021/04/16 10:33 dvelegra
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

import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.ws.controller.correspondence.CorrespondenceDownloadController;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CorrespondenceDownloadControllerTest {

    @InjectMocks
    CorrespondenceDownloadController correspondenceDownloadController;

    @Mock
    private PdfService pdfService;

    private Authentication authentication;
    private static final String USERNAME = "abc@xyz.com";
    private static final String MESSAGE_ID = "123";

    @Before
    public void setUp() {
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(USERNAME);
    }

    @Test
    public void testGetCorrespondenceMessageZip() {
        final String fileName = "message.pdf";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(pdfService.generatePdf(authentication.getName(), MESSAGE_ID, false)).thenReturn(fileInfo);
        ResponseEntity<byte[]> result = correspondenceDownloadController.getZip(authentication, MESSAGE_ID);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        byte[] response = result.getBody();
        assertNotNull(response);
    }


    @Test
    public void testGetCorrespondenceDraft() {
        final String fileName = "draft.pdf";
        final byte[] content = {2, 5, -2, 8, 3};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(pdfService.generatePdf(authentication.getName(), MESSAGE_ID, true)).thenReturn(fileInfo);
        ResponseEntity<byte[]> result = correspondenceDownloadController.getDraftPdf(authentication, MESSAGE_ID);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        byte[] response = result.getBody();
        assertNotNull(response);
    }
}