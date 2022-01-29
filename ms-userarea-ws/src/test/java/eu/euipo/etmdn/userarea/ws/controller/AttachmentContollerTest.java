/*
 * $Id:: AttachmentContollerTest.java 2021/04/06 01:05 tantonop
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

package eu.euipo.etmdn.userarea.ws.controller;

import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageAttachmentService;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.DraftAttachmentResource;
import eu.euipo.etmdn.userarea.ws.controller.correspondence.AttachmentController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class AttachmentContollerTest {

    @InjectMocks
    AttachmentController attachmentController;

    @Mock
    MessageAttachmentService messageAttachmentService;
    private Authentication authentication;
    private static final String username = "abc@xyz.com";

    @Before
    public void setUp() {
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
    }

    @Test
    public void shouldCreateNewMessageAttachment() {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        MockMultipartFile firstFile = new MockMultipartFile("launch-plan/src/main/resources/userarea/config/backend/data", "filename.txt", "text/plain", "some xml".getBytes());
        multipartFiles.add(firstFile);
        ResponseEntity<List<DraftAttachmentResource>> resp = attachmentController.createAttachment(authentication,"1",multipartFiles);
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void shouldGetAttachment() {
        FileResponse fileResponse = AbstractMother.random(FileResponse.class);
        fileResponse.setBytes("some xml".getBytes());
        fileResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        when(messageAttachmentService.getAttachment(any(),any(),any(Boolean.class))).thenReturn(fileResponse);
        ResponseEntity<byte[]> resp = attachmentController.getAttachment(authentication,"1");
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void shouldGetDraftAttachments() {
        FileResponse fileResponse = AbstractMother.random(FileResponse.class);
        fileResponse.setBytes("some xml".getBytes());
        fileResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        when(messageAttachmentService.getAttachment(any(), any(), any(Boolean.class))).thenReturn(fileResponse);
        ResponseEntity<byte[]> response = attachmentController.getDraftAttachment(authentication, "1");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldDeleteAttachment() {
        ResponseEntity<Void> resp = attachmentController.deleteAttachment(authentication,"1");
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

}
