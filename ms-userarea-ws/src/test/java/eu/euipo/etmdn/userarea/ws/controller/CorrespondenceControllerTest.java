/*
 * $Id:: CorrespondenceControllerTest.java 2021/04/06 01:08 tantonop
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
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageSearchResultContent;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageResource;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageSearchResultResource;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.SearchCriteriaResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.MessageResourceMapper;
import eu.euipo.etmdn.userarea.ws.config.SPBackOfficeConfiguration;
import eu.euipo.etmdn.userarea.ws.controller.correspondence.CorrespondenceController;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CorrespondenceControllerTest {

    @InjectMocks
    CorrespondenceController messageController;

    @Mock
    MessageService messageService;
    @Mock
    SPBackOfficeConfiguration spBackOfficeConfiguration;

    private Authentication authentication;

    @Before
    public void setUp() {
        MessageResource messageResource = AbstractMother.random(MessageResource.class);
        Message message = MessageResourceMapper.MAPPER.map(messageResource);
        message.setMessageStatus(MessageStatus.NEW);
        authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("test@test.com");
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CORRESPONDENCE_READ_ONLY"));
        when(authentication.getAuthorities()).thenReturn(authorities);
    }

    @Test
    public void shouldReturnSearchResults(){
        SearchCriteriaResource resource = AbstractMother.random(SearchCriteriaResource.class);
        ResponseEntity<MessageSearchResultResource> resp = messageController.getAllMessages(authentication, resource);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp);
    }

    @Test
    public void shouldReturnMessageDetails(){
        String id = "11";
        SearchCriteriaResource resource = AbstractMother.random(SearchCriteriaResource.class);;
        ResponseEntity<MessageSearchResultContent> resp = messageController.getCorrespondenceDetails(authentication, id, resource);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp);
    }

    @Test
    public void shouldReturnNewCorrespondenceCounter(){
        when(messageService.getNewIncomingCorrespondence(authentication.getName(), AuthorityUtils.authorityListToSet(authentication.getAuthorities()))).thenReturn(5);
        ResponseEntity<String> resp = messageController.getNewIncomingMessageCount(authentication);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp);
    }

    @Test
    public void shouldGetResultsForExpiringCorrespondence(){
        SearchCriteriaResource resource = AbstractMother.random(SearchCriteriaResource.class);
        ResponseEntity<String> resp = messageController.getExpiringMessagesCount(authentication, resource);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp);
    }

}
