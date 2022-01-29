/*
 * $Id:: DraftControllerTest.java 2021/04/06 01:05 tantonop
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
import eu.euipo.etmdn.userarea.common.business.correspondence.DraftService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Draft;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.DraftResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.DraftResourceMapper;
import eu.euipo.etmdn.userarea.ws.config.SPBackOfficeConfiguration;
import eu.euipo.etmdn.userarea.ws.controller.correspondence.DraftController;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class DraftControllerTest {

    @InjectMocks
    DraftController draftController;

    @Mock
    private DraftService draftService;

    @Mock
    private SPBackOfficeConfiguration backOfficeConfiguration;

    private DraftResource draftResource;
    private Authentication authentication;
    private static final String username = "abc@xyz.com";

    @Before
    public void setUp() {
        DraftResource draftResource = AbstractMother.random(DraftResource.class);
        Draft draft = DraftResourceMapper.MAPPER.map(draftResource);
        Message message = AbstractMother.random(Message.class);
        draft.setMessage(message);
        this.draftResource = draftResource;
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        when(draftService.create(username, 11L)).thenReturn(draft);
        when(draftService.update(any(String.class), any(Long.class), any(Draft.class), any(Boolean.class), any(Boolean.class))).thenReturn(draft);
        when(draftService.get("11")).thenReturn(draft);
        when(backOfficeConfiguration.getServiceBackOffice()).thenReturn("test");
        when(backOfficeConfiguration.getDraftBackOffice()).thenReturn("test");
        when(draftService.sendDraftReply(any(String.class), any(Long.class), any(Draft.class), any(List.class), any(String.class))).thenReturn(draft);
    }

    @Test
    public void shouldCreateNewDraft() {
        ResponseEntity<DraftResource> resp = draftController.createDraft(authentication, "11");
        assertNotNull(resp);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    }

    @Test
    public void shouldUpdateDraft() {
        ResponseEntity<DraftResource> resp = draftController.updateDraft(authentication, draftResource);
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void shouldGetDraft() {
        ResponseEntity<DraftResource> resp = draftController.get("11");
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void shouldDeleteDraft() {
        ResponseEntity<Void> resp = draftController.deleteDraft(authentication,"11");
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void shouldCheckDraftIsLocked() {
        ResponseEntity<Boolean> resp = draftController.getIsLocked(authentication,"11");
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    public void shouldSendDraftReply() {
        ResponseEntity<Void> resp = draftController.sendDraftReply(authentication, draftResource, new ArrayList<>());
        assertNotNull(resp);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

}
