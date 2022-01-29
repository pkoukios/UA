/*
 * $Id:: DraftControllerTest.java 2021/03/11 08:50 tantonop
 * . * .
 *  RRRR * Copyright (c) 2012-2021 EUIPO: European Intelectual
 * . RR R . Property Organization (trademarks and designs).
 *  RRR *
 * . RR RR . ALL RIGHTS RESERVED
 * . _ .*
 * The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.common.business.correspondence.DraftService;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Draft;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.DraftEntity;
import eu.euipo.etmdn.userarea.common.persistence.mapper.correspondence.DraftMapper;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.DraftRepository;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.DraftResource;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.DraftResourceMapper;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.MessageResourceMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.jcr.Repository;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
@Slf4j
public class DraftControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private DraftService draftService;

    @MockBean
    private DraftRepository draftRepository;

    @MockBean
    private MessageService messageService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private Authentication authentication;

    @Mock
    private RestTemplate restTemplate;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoDraft() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When a draft is received, it returns the created Draft")
    public void shouldCreateNewDraft() throws Exception {
        Draft draft = AbstractMother.random(Draft.class);
        DomainAccount account = AbstractMother.random(DomainAccount.class);
        MessageResource messageResource = AbstractMother.random(MessageResource.class);
        Message message = MessageResourceMapper.MAPPER.map(messageResource);
        message.setMessageStatus(MessageStatus.NEW);
        message.setRecipientId("test");
        doReturn(draft).when(draftService).create(any(String.class), any(Long.class));
        DraftResource ret = DraftResourceMapper.MAPPER.map(draft);
        DraftEntity entity = DraftMapper.MAPPER.map(draft);
        when(authentication.getName()).thenReturn("test");
        when(accountService.getMainAccount(any())).thenReturn(account);
        when(accountService.isAllowedToModifyDraft(any(), any())).thenReturn(true);
        when(messageService.getByIdAndLock(any(), any())).thenReturn(message);
        when(draftRepository.save(any())).thenReturn(entity);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        String jsonStrRet = mapper.writeValueAsString(ret);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("messageId", "11");
        this.mockMvc.perform(
                post("/correspondences/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(params)
                        .with(csrf())
                        .characterEncoding("UTF-8"))
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonStrRet))
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When a draft is received, it returns the created Draft")
    public void shouldUpdateDraft() throws Exception {
        String jsonString = "{\"id\":1, \"messageId\":11,\"body\":\"Hello\"}";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        Draft draft = AbstractMother.random(Draft.class);
        draft.getMessage().setDueDate(tomorrow);
        DomainAccount account = AbstractMother.random(DomainAccount.class);
        MessageResource messageResource = AbstractMother.random(MessageResource.class);
        Message message = MessageResourceMapper.MAPPER.map(messageResource);
        message.setMessageStatus(MessageStatus.NEW);
        when(accountService.isAllowedToModifyDraft(any(), any())).thenReturn(true);
        doReturn(draft).when(draftService).update(any(String.class), any(Long.class), any(Draft.class), any(Boolean.class), any(Boolean.class));
        DraftEntity entity = DraftMapper.MAPPER.map(draft);
        Optional<DraftEntity> entityOptional = Optional.of(entity);
        entity.setUser("test");
        draft.setUser("test");
        draft.setBody("Hello");
        DraftResource ret = DraftResourceMapper.MAPPER.map(draft);
        draft.setActionDate(ret.getActionDate());
        when(authentication.getName()).thenReturn("test");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        String jsonStrRet = mapper.writeValueAsString(ret);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("messageId", "11");
        when(draftService.update(any(), any(), any(), any(Boolean.class), any(Boolean.class))).thenReturn(draft);
        when(draftRepository.save(any())).thenReturn(entity);
        when(draftRepository.findById(any())).thenReturn(entityOptional);
        when(messageService.checkIfMessageLockedBySameUser(any(), any())).thenReturn(false);
        when(accountService.getMainAccount(any())).thenReturn(account);
        this.mockMvc.perform(
                put("/correspondences/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonString)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When a draft is received, it returns the created Draft")
    public void shouldDeleteDraft() throws Exception {
        Draft draft = AbstractMother.random(Draft.class);
        draft.setMessage(Message.builder().id(10L).externalRef("12345").build());
        DraftEntity entity = DraftMapper.MAPPER.map(draft);
        entity.setId(1L);
        Optional<DraftEntity> entityOptional = Optional.of(entity);
        when(authentication.getName()).thenReturn("test");
        when(accountService.isAllowedToModifyDraft(any(), any())).thenReturn(true);
        when(draftRepository.findById(any())).thenReturn(entityOptional);
        this.mockMvc.perform(
                delete("/correspondences/draft/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When a draft is ready, send draft as reply")
    public void shouldSendDraftReply() throws Exception {
        Draft draft = AbstractMother.random(Draft.class);
        draft.setMessage(Message.builder().id(10L).externalRef("12345").dueDate(LocalDateTime.now().plusDays(1)).build());
        DraftEntity entity = DraftMapper.MAPPER.map(draft);
        Optional<DraftEntity> entityOptional = Optional.of(entity);
        DomainAccount account = AbstractMother.random(DomainAccount.class);
        when(authentication.getName()).thenReturn("test");
        when(draftService.update(any(), any(), any(), any(Boolean.class), any(Boolean.class))).thenReturn(draft);
        when(draftRepository.save(any())).thenReturn(entity);
        when(draftRepository.findById(any())).thenReturn(entityOptional);
        when(messageService.checkIfMessageLockedBySameUser(any(), any())).thenReturn(false);
        when(accountService.isAllowedToModifyDraft(any(), any())).thenReturn(true);
        when(accountService.getMainAccount(any())).thenReturn(account);
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(any());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        DraftResource draftResource = DraftResourceMapper.MAPPER.map(draft);
        String jsonStr = mapper.writeValueAsString(draftResource);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("draftResource", jsonStr);
        this.mockMvc.perform(
                post("/correspondences/draft/send")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(params)
                        .content(jsonStr)
                        .characterEncoding("UTF-8")
                        .with(csrf()));
    }
}
