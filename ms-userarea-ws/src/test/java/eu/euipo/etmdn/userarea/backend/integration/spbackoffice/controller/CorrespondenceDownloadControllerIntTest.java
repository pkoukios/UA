/*
 * $Id:: CorrespondenceDownloadControllerIntTest.java 2021/04/06 04:23 tantonop
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

import eu.euipo.etmdn.userarea.backend.integration.spbackoffice.domain.AbstractMother;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.business.config.PdfTemplateConfiguration;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageAttachmentService;
import eu.euipo.etmdn.userarea.common.business.helper.PdfTemplateHelper;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Draft;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Message;
import eu.euipo.etmdn.userarea.common.persistence.document.DocumentClient;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.DraftEntity;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.MessageEntity;
import eu.euipo.etmdn.userarea.common.persistence.mapper.correspondence.DraftMapper;
import eu.euipo.etmdn.userarea.common.persistence.mapper.correspondence.MessageMapper;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.DraftRepository;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.MessageRepository;
import eu.euipo.etmdn.userarea.ws.controller.ApplicationController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class CorrespondenceDownloadControllerIntTest {

    private static final String OPENAPI_YAML = "openapi/openapi.yaml";

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private PdfService pdfService;
    @MockBean
    private Authentication authentication;
    @MockBean
    private AccountService accountService;
    @MockBean
    private ApplicationController applicationController;
    @MockBean
    private DocumentClient documentClient;
    @MockBean
    private MessageAttachmentService messageAttachmentService;
    @Mock
    private RestTemplate restTemplate;
    @MockBean
    private PdfTemplateConfiguration pdfTemplateConfiguration;
    @MockBean
    private PdfTemplateHelper pdfTemplateHelper;
    @MockBean
    private DraftRepository draftRepository;
    @MockBean
    private MessageRepository messageRepository;

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When a message download is requested then the message is downloaded")
    public void whenMessageDownloadIsRequestedTheMessageIdDownloaded() throws Exception {
        Message message = AbstractMother.random(Message.class);
        DomainAccount domainAccount = AbstractMother.random(DomainAccount.class);
        MessageEntity messageEntity = MessageMapper.MAPPER.map(message);
        when(authentication.getName()).thenReturn("test");
        FileInfo fileResponse = AbstractMother.random(FileInfo.class);
        fileResponse.setFileContent("some zip".getBytes());
        fileResponse.setFileName("filename.zip");
        when(pdfService.generatePdf("test", "33", false)).thenReturn(fileResponse);
        when(pdfTemplateHelper.processTemplate(any(), any(), any())).thenReturn("<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"row\">\n" +
                "</div>\n" +
                "<div class=\"toprowmiddle\">\n" +
                "\n" +
                "            <h1 style=\"text-align: center; padding-bottom: 30px;\">\n" +
                "                <span>some text      </span>\n" +
                "            </h1>\n" +
                "\n" +
                "</div></body>\n" +
                "</html>");
        when(messageRepository.getOne(any())).thenReturn(messageEntity);
        when(accountService.getMainAccount(any())).thenReturn(domainAccount);
        this.mockMvc.perform(
                get("/correspondences/download/incoming/33").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

    @Test
    @WithMockUser(username = "test", roles = "CORRESPONDENCE_READ_WRITE")
    @DisplayName("When a draft download is requested then the draft is downloaded")
    public void whenDraftDownloadIsRequestedTheMessageIdDownloaded() throws Exception {
        Draft draft = AbstractMother.random(Draft.class);
        DomainAccount domainAccount = AbstractMother.random(DomainAccount.class);
        DraftEntity draftEntity = DraftMapper.MAPPER.map(draft);
        when(authentication.getName()).thenReturn("test");
        FileInfo fileResponse = AbstractMother.random(FileInfo.class);
        fileResponse.setFileContent("some pdf".getBytes());
        fileResponse.setFileName("filename.pdf");
        when(pdfService.generatePdf(any(), any(), any(Boolean.class))).thenReturn(fileResponse);
        when(pdfTemplateHelper.processTemplate(any(), any(), any())).thenReturn("<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"row\">\n" +
                "</div>\n" +
                "<div class=\"toprowmiddle\">\n" +
                "\n" +
                "            <h1 style=\"text-align: center; padding-bottom: 30px;\">\n" +
                "                <span>some text      </span>\n" +
                "            </h1>\n" +
                "\n" +
                "</div></body>\n" +
                "</html>");
        when(draftRepository.getOne(any())).thenReturn(draftEntity);
        when(accountService.getMainAccount(any())).thenReturn(domainAccount);
        this.mockMvc.perform(
                get("/correspondences/download/draft/33").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(OPENAPI_YAML));
    }

}
