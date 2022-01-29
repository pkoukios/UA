/*
 * $Id:: AttachmentControllerTest.java 2021/04/01 04:26 tantonop
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

package eu.euipo.etmdn.userarea.backend.integration.spbackoffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageAttachmentService;
import eu.euipo.etmdn.userarea.common.domain.correspondence.DraftAttachment;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.ws.domain.correspondence.DraftAttachmentResource;
import eu.euipo.etmdn.userarea.common.ws.mapper.correspondence.DraftAttachmentResourceMapper;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.jcr.Repository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AttachmentControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageAttachmentService messageAttachmentService;

    @MockBean
    private Authentication authentication;

    @TestConfiguration
    static class TestRepositoryConfiguration {

        @Bean
        public Repository getRepoAttachment() {
            MemoryNodeStore ns = new MemoryNodeStore();
            return new Jcr(new Oak(ns)).createRepository();
        }
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When attachment is being send, attachment is saved")
    public void shouldCreateNewAttachment() throws Exception {
        when(authentication.getName()).thenReturn("test");
        DraftAttachment draftAttachment = AbstractMother.random(DraftAttachment.class);
        ArrayList<DraftAttachmentResource> ret = new ArrayList<>();
        ArrayList<DraftAttachment> draftAttachments = new ArrayList<>();
        draftAttachments.add(draftAttachment);
        DraftAttachmentResource resource = DraftAttachmentResourceMapper.MAPPER.map(draftAttachment);
        ret.add(resource);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        String jsonStr = mapper.writeValueAsString(ret);
        when(messageAttachmentService.createDraftAttachments(any(), any(), any())).thenReturn(draftAttachments);
        MockMultipartFile firstFile = new MockMultipartFile("launch-plan/src/main/resources/userarea/config/backend/data", "filename.txt", "text/plain", "some xml".getBytes());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/correspondences/attachments/1")
                .file(firstFile).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonStr));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When attachment is requested then data is returned")
    public void shouldGetAttachment() throws Exception {
        when(authentication.getName()).thenReturn("test");
        FileResponse fileResponse = AbstractMother.random(FileResponse.class);
        fileResponse.setBytes("some xml".getBytes());
        fileResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        when(messageAttachmentService.getAttachment(any(), any(), any(Boolean.class))).thenReturn(fileResponse);
        this.mockMvc.perform(
                get("/correspondences/attachments/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileResponse.getBytes()))
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When attachment is requested the data is returned")
    public void shouldGetDraftAttachment() throws Exception {
        when(authentication.getName()).thenReturn("test");
        FileResponse fileResponse = AbstractMother.random(FileResponse.class);
        fileResponse.setBytes("some xml".getBytes());
        fileResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        when(messageAttachmentService.getAttachment(any(), any(), any(Boolean.class))).thenReturn(fileResponse);
        this.mockMvc.perform(
                get("/correspondences/attachments/draft/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileResponse.getBytes()))
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When request for delete than attachment is deleted")
    public void shouldDeleteAttachment() throws Exception {
        when(authentication.getName()).thenReturn("test");
        this.mockMvc.perform(
                delete("/correspondences/attachments/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

}
