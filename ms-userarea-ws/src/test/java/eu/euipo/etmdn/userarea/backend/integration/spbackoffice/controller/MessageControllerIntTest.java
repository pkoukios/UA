/*
 * $Id:: MessageControllerIntTest.java 2021/05/12 04:30 dvelegra
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
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.DraftSearch;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageSearchResult;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageSearchResultContent;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.domain.correspondence.search.FilterCriteria;
import eu.euipo.etmdn.userarea.common.domain.correspondence.search.SearchCriteria;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.MessageResource;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.SearchCriteriaResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.MessageResourceMapper;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.SearchCriteriaResourceMapper;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.jcr.Repository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private Authentication authentication;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoMessage() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @DisplayName("When a message is received, it returns the created Message")
    public void shouldCreateNewMessage() throws Exception {
        MessageResource messageResource = AbstractMother.random(MessageResource.class);
        Message message = MessageResourceMapper.MAPPER.map(messageResource);
        message.setMessageStatus(MessageStatus.NEW);
        doReturn(message).when(messageService).save(any(), any());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        String jsonStr = mapper.writeValueAsString(messageResource);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("messageResource", jsonStr);
        this.mockMvc.perform(
                post("/correspondences/message")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(params)
                        .with(csrf())
                        .content(jsonStr)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonStr))
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @DisplayName("When a message is received with wrong due date, it returns bad request")
    public void badRequestWhenDueDateFormatIsWrong() throws Exception {
        String request = "{\"externalRef\":\"eOMtThyh\",\"procedure\":\"VNLWUZNRc\",\"subject\":\"BaQKx\",\"applicationId\":\"IyedUsF\",\"dueDate\":\"wrondDate\",\"actionDate\":\"2020-12-30 23:46:52\",\"requiresReply\":true,\"body\":\"wdkelQbx\",\"html\":false}";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("messageResource", request);
        this.mockMvc.perform(
                post("/correspondences/message")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(params)
                        .with(csrf())
                        .content(request)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("When a message is received with action date, it returns bad request")
    public void badRequestWhenBooleanIsWrong() throws Exception {
        String request = "{\"externalRef\":\"eOMtThyh\",\"procedure\":\"VNLWUZNRc\",\"subject\":\"BaQKx\",\"applicationId\":\"IyedUsF\",\"dueDate\":\"2020-12-30 23:46:52\",\"actionDate\":\"wrongDate\",\"requiresReply\":true,\"body\":\"wdkelQbx\",\"html\":true}";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("messageResource", request);
        this.mockMvc.perform(
                post("/correspondences/message")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .params(params)
                        .with(csrf())
                        .content(request)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CORRESPONDENCE_READ_ONLY")
    @DisplayName("When user searches for correspondence messages then we get results")
    public void whenUserSearchesForCorrespondenceMessagesThenWeGetResults() throws Exception {
        SearchCriteriaResource searchCriteriaResource = AbstractMother.random(SearchCriteriaResource.class);
        MessageSearchResult resultResource = AbstractMother.random(MessageSearchResult.class);
        doReturn(resultResource).when(messageService).getAllCorrespondencePerSearchCriteria(any(String.class), any(SearchCriteria.class));
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        String jsonStr = mapper.writeValueAsString(searchCriteriaResource);
        this.mockMvc.perform(
                post("/correspondences/message/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStr)
                        .with(csrf())
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_ONLY")
    @DisplayName("When user asks for the new UNREAD messages a count is being returned")
    public void whenUserAsksForTheNewUnreadMessagesCountIsBeingReturned() throws Exception {
        int result = 2;
        doReturn(result).when(messageService).getNewIncomingCorrespondence("test", new HashSet<>(Collections.singleton("ROLE_CORRESPONDENCE_READ_ONLY")));
        when(authentication.getName()).thenReturn("test");
        this.mockMvc.perform(
                get("/correspondences/message/incoming/new").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(result)))
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_ONLY")
    @DisplayName("When message details are requested the details are returned ")
    public void whenMessageDetailsAreRequestedTheDetailsAreReturned() throws Exception {
        Message message = AbstractMother.random(Message.class);
        MessageSearchResultContent content = AbstractMother.random(MessageSearchResultContent.class);
        DraftSearch draftSearch = AbstractMother.random(DraftSearch.class);
        List<DraftSearch> draftSearches = new ArrayList<>();
        draftSearches.add(draftSearch);
        content.setSubRows(draftSearches);
        SearchCriteriaResource searchCriteriaResource = AbstractMother.random(SearchCriteriaResource.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        String jsonStr = mapper.writeValueAsString(searchCriteriaResource);
        String id = "2";
        doReturn(content).when(messageService).getMessageDetails(eq("test"), eq(id), any(String.class), any(String.class), any(SearchCriteria.class));
        when(authentication.getName()).thenReturn("test");
        this.mockMvc.perform(
                post("/correspondences/message/detail/2")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(jsonStr).
                        with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_ONLY")
    @DisplayName("When user asks for the number of expiring messages a count is being returned")
    public void whenUserAsksForTheNumberOfExpiringMessagesCountIsBeingReturned() throws Exception {
        int result = 2;
        when(messageService.getExpiringCorrespondences(eq("test"), any(Set.class), any(FilterCriteria.class))).thenReturn(result);
        when(authentication.getName()).thenReturn("test");
        SearchCriteria searchCriteria = AbstractMother.random(SearchCriteria.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        SearchCriteriaResource searchCriteriaResource = SearchCriteriaResourceMapper.MAPPER.map(searchCriteria);
        String jsonStr = mapper.writeValueAsString(searchCriteriaResource);
        this.mockMvc.perform(
                post("/correspondences/message/expiring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStr)
                        .with(csrf())
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(result)));
    }
}
