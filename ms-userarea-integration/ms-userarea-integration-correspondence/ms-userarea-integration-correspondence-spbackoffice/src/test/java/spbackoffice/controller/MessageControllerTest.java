/*
 * $Id:: MessageControllerTest.java 2021/05/13 01:46 dvelegra
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

package spbackoffice.controller;

import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller.MessageController;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.MessageResourceMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class MessageControllerTest {

    @InjectMocks
    MessageController messageController;

    @Mock
    private MessageService messageService;

    private MessageResource messageResource;



    @Before
    public void setUp() {
        MessageResource messageResource = AbstractMother.random(MessageResource.class);
        Message message = MessageResourceMapper.MAPPER.map(messageResource);
        message.setMessageStatus(MessageStatus.NEW);
        this.messageResource = messageResource;
    }

    @Test
    public void shouldCreateNewMessage() {
        ResponseEntity<MessageResource> resp = messageController.create(messageResource,new ArrayList<>());
        assertNotNull(resp);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    }

}
