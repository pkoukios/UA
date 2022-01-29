/*
 * $Id:: PdfServiceImplTest.java 2021/09/01 11:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service.pdf;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.business.config.PdfTemplateConfiguration;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageAttachmentService;
import eu.euipo.etmdn.userarea.common.business.helper.PdfTemplateHelper;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.AccountType;
import eu.euipo.etmdn.userarea.common.domain.ApplicantType;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.FilteringData;
import eu.euipo.etmdn.userarea.common.domain.MainAccountType;
import eu.euipo.etmdn.userarea.common.domain.PaginationData;
import eu.euipo.etmdn.userarea.common.domain.PdfTemplate;
import eu.euipo.etmdn.userarea.common.domain.PdfTemplateType;
import eu.euipo.etmdn.userarea.common.domain.SortingData;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.domain.exception.InvalidDownloadAttachmentUserException;
import eu.euipo.etmdn.userarea.common.persistence.document.DocumentClient;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.DraftEntity;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.MessageEntity;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.DraftRepository;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.MessageRepository;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PdfServiceImplTest {

    @InjectMocks
    private PdfServiceImpl pdfService;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private PdfTemplateConfiguration pdfTemplateConfiguration;
    @Mock
    private PdfTemplateHelper pdfTemplateHelper;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private DraftRepository draftRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private IpoConfiguration ipoConfiguration;
    @Mock
    private MessageAttachmentService messageAttachmentService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private DocumentClient documentClient;

    private ApplicationSearchResult pageableApplicationTrademark;
    private ApplicationSearchResult pageableApplicationDesign;
    private ApplicationSearchResult pageableApplicationEservice;
    private ApplicationRequest applicationRequestTrademark;
    private ApplicationRequest applicationRequestDesign;
    private ApplicationRequest applicationRequestEservice;
    private DomainAccount account;
    private DomainAccount currentAccount;
    private DraftEntity draftEntity;
    private PdfTemplate pdfTemplate;
    private final Set<String> roles = new HashSet<>();
    private static final String USERNAME = "abc@xyz.com";
    private static final String accountUsername = "johnDoe@doe.com";
    private static final String content = "<!DOCTYPE html><html><body><div><main></main></div></body></html>";

    @Before
    public void setUp() {
        pdfService = new PdfServiceImpl(pdfTemplateConfiguration, pdfTemplateHelper, applicationService,messageRepository,draftRepository,accountService,ipoConfiguration,messageAttachmentService, paymentService, documentClient, "");
        ApplicationDetails applicationTrademark = ApplicationDetails.builder().type("Word").kind("Individual").status("Submitted").build();
        pageableApplicationTrademark = new ApplicationSearchResult();
        pageableApplicationTrademark.setContent(Collections.singletonList(applicationTrademark));
        applicationRequestTrademark = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.TRADEMARK.value)
                .columns(Arrays.asList("Number", "Kind", "Type"))
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().types(Collections.singletonList("Word")).build())
                .searchingData(null)
                .build();
        ApplicationDetails applicationDesign = ApplicationDetails.builder().status("Submitted").build();
        pageableApplicationDesign = new ApplicationSearchResult();
        pageableApplicationDesign.setContent(Collections.singletonList(applicationDesign));
        applicationRequestDesign = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.DESIGN.value)
                .columns(Arrays.asList("Number", "Indication"))
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().statuses(Collections.singletonList("Submitted")).build())
                .searchingData(null)
                .build();
        ApplicationDetails applicationEservice = ApplicationDetails.builder().ipRightType("Trademarks").status("Submitted").build();
        pageableApplicationEservice = new ApplicationSearchResult();
        pageableApplicationEservice.setContent(Collections.singletonList(applicationEservice));
        applicationRequestEservice = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.ESERVICE.value)
                .columns(Arrays.asList("Number", "EserviceName"))
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().statuses(Collections.singletonList("Submitted")).build())
                .searchingData(null)
                .build();
        roles.add("ROLE_TRADEMARKS");
        pdfTemplate = new PdfTemplate();
        pdfTemplate.setPath("Pdf");
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setId(1L);
        this.draftEntity = new DraftEntity();
        this.draftEntity.setUser("abc@xyz.com");
        this.draftEntity.setId(1L);
        this.draftEntity.setActionDate(LocalDateTime.now());
        this.draftEntity.setCreatedDate(LocalDateTime.now());
        this.draftEntity.setMessage(messageEntity);
        this.draftEntity.setBody("Test message");
        this.draftEntity.getMessage().setDueDate(LocalDateTime.now());
        this.draftEntity.getMessage().setActionDate(LocalDateTime.now());
        this.draftEntity.getMessage().setDueDate(LocalDateTime.now());
        this.draftEntity.setDraftStatus(MessageStatus.NEW.getValue());
        this.account = DomainAccount.builder().id(1L).username(USERNAME).firstName("John").surName("Cool")
                .type(AccountType.PARENT.name())
                .mainAccountType(MainAccountType.APPLICANT.getValue())
                .legalType(ApplicantType.INDIVIDUAL.getValue()).build();
        this.currentAccount = DomainAccount.builder().id(1L).username("johnDoe@doe.com").firstName("John").surName("Cool")
                .type(AccountType.PARENT.name())
                .mainAccountType(MainAccountType.APPLICANT.getValue())
                .legalType(ApplicantType.INDIVIDUAL.getValue()).build();
    }

    @Test
    public void testGenerateTrademarkToPdf() {
        applicationRequestTrademark.setIsDraft(false);
        when(applicationService.getApplications(applicationRequestTrademark, roles)).thenReturn(pageableApplicationTrademark);
        when(pdfTemplateConfiguration.getPdfTemplate(PdfTemplateType.TRADEMARK)).thenReturn(pdfTemplate);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo fileInfo = pdfService.generatePdf(applicationRequestTrademark, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDesignToPdf() {
        applicationRequestDesign.setIsDraft(false);
        when(applicationService.getApplications(applicationRequestDesign, roles)).thenReturn(pageableApplicationDesign);
        when(pdfTemplateConfiguration.getPdfTemplate(PdfTemplateType.DESIGN)).thenReturn(pdfTemplate);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo fileInfo = pdfService.generatePdf(applicationRequestDesign, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateEserviceToPdf() {
        applicationRequestEservice.setIsDraft(false);
        when(applicationService.getApplications(applicationRequestEservice, roles)).thenReturn(pageableApplicationEservice);
        when(pdfTemplateConfiguration.getPdfTemplate(PdfTemplateType.ESERVICE)).thenReturn(pdfTemplate);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo fileInfo = pdfService.generatePdf(applicationRequestEservice, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDraftTrademarkToPdf() {
        applicationRequestTrademark.setIsDraft(true);
        when(applicationService.getApplications(applicationRequestTrademark, roles)).thenReturn(pageableApplicationTrademark);
        when(pdfTemplateConfiguration.getPdfTemplate(PdfTemplateType.TRADEMARK_DRAFT)).thenReturn(pdfTemplate);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo fileInfo = pdfService.generatePdf(applicationRequestTrademark, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDraftDesignToPdf() {
        applicationRequestDesign.setIsDraft(true);
        when(applicationService.getApplications(applicationRequestDesign, roles)).thenReturn(pageableApplicationDesign);
        when(pdfTemplateConfiguration.getPdfTemplate(PdfTemplateType.DESIGN_DRAFT)).thenReturn(pdfTemplate);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo fileInfo = pdfService.generatePdf(applicationRequestDesign, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDraftEserviceToPdf() {
        applicationRequestEservice.setIsDraft(true);
        when(applicationService.getApplications(applicationRequestEservice, roles)).thenReturn(pageableApplicationEservice);
        when(pdfTemplateConfiguration.getPdfTemplate(PdfTemplateType.ESERVICE_DRAFT)).thenReturn(pdfTemplate);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo fileInfo = pdfService.generatePdf(applicationRequestEservice, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDownloadPdf() {
        when(draftRepository.getOne(eq(1L))).thenReturn(this.draftEntity);
        when(accountService.getMainAccount(USERNAME)).thenReturn(account);
        when(pdfTemplateHelper.processTemplate(any(), any(), eq(Locale.getDefault()))).thenReturn(content);
        FileInfo info = pdfService.generatePdf(USERNAME, "1", true);
        assertNotNull(info);
    }

    @Test(expected = InvalidDownloadAttachmentUserException.class)
    public void testGenerateDownloadPdfException() {
        when(draftRepository.getOne(eq(1L))).thenReturn(this.draftEntity);
        when(accountService.getAccountByUsername(accountUsername)).thenReturn(currentAccount);
        when(accountService.getAccountByUsername(USERNAME)).thenReturn(account);
        pdfService.generatePdf(accountUsername, "1", true);
    }
}