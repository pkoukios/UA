/*
 * $Id:: ApplicationControllerTest.java 2021/03/02 02:09 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 */

package eu.euipo.etmdn.userarea.ws.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.ExcelService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.AccountType;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DraftResponse;
import eu.euipo.etmdn.userarea.common.domain.DuplicateApplicationResponse;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.FilteringData;
import eu.euipo.etmdn.userarea.common.domain.PaginationData;
import eu.euipo.etmdn.userarea.common.domain.Receipt;
import eu.euipo.etmdn.userarea.common.domain.ResumeDraftResponse;
import eu.euipo.etmdn.userarea.common.domain.SortingData;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceRequest;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidationStatusEServiceType;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import eu.euipo.etmdn.userarea.ws.domain.application.ApplicationRequestResource;
import eu.euipo.etmdn.userarea.ws.domain.application.ApplicationSearchResultResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.DESIGN;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.ESERVICE;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.IP_RIGHT_TYPE_TRADEMARKS;
import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TRADEMARK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ApplicationControllerTest {

    @InjectMocks
    private ApplicationController applicationController;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private AccountService accountService;
    @Mock
    private ExcelService excelService;
    @Mock
    private PdfService pdfService;

    private DomainAccount mainAccount;
    private ApplicationSearchResult pageableApplicationTrademark;
    private ApplicationSearchResult pageableApplicationDesign;
    private ApplicationSearchResult pageableApplicationEservice;
    private ApplicationRequestResource applicationRequestResourceTrademark;
    private ApplicationRequestResource applicationRequestResourceDesign;
    private ApplicationRequestResource applicationRequestResourceEservice;
    private ApplicationRequest applicationRequestTrademark;
    private ApplicationRequest applicationRequestDesign;
    private ApplicationRequest applicationRequestEservice;
    private NoteApplication noteApplication;
    private Authentication authentication;
    private final Set<String> roles = new HashSet<>();

    private static final String frontOfficeUrl = "https://localhost:8443";
    private static final String USERNAME = "abc@xyz.com";
    private static final String NOTE = "Test note";
    private static final String NUMBER = "12345678";

    @Before
    public void setUp() {
        applicationController = new ApplicationController(applicationService, accountService, excelService, pdfService);
        ApplicationDetails applicationTrademark = ApplicationDetails.builder().type("Word").kind("Individual").status("Submitted").build();
        pageableApplicationTrademark = new ApplicationSearchResult();
        pageableApplicationTrademark.setContent(Collections.singletonList(applicationTrademark));
        applicationRequestResourceTrademark = ApplicationRequestResource.builder()
                .userName(USERNAME)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().types(Collections.singletonList("Word")).build())
                .searchingData(null)
                .build();
        applicationRequestTrademark = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.TRADEMARK.value)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().types(Collections.singletonList("Word")).build())
                .searchingData(null)
                .build();
        ApplicationDetails applicationDesign = ApplicationDetails.builder().status("Submitted").build();
        pageableApplicationDesign = new ApplicationSearchResult();
        pageableApplicationDesign.setContent(Collections.singletonList(applicationDesign));
        applicationRequestResourceDesign = ApplicationRequestResource.builder()
                .userName(USERNAME)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().statuses(Collections.singletonList("Submitted")).build())
                .searchingData(null)
                .build();
        applicationRequestDesign = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.DESIGN.value)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().statuses(Collections.singletonList("Submitted")).build())
                .searchingData(null)
                .build();
        ApplicationDetails applicationEservice = ApplicationDetails.builder().ipRightType("Trademarks").status("Submitted").build();
        pageableApplicationEservice = new ApplicationSearchResult();
        pageableApplicationEservice.setContent(Collections.singletonList(applicationEservice));
        applicationRequestResourceEservice = ApplicationRequestResource.builder()
                .userName(USERNAME)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().statuses(Collections.singletonList("Submitted")).build())
                .searchingData(null)
                .build();
        applicationRequestEservice = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.ESERVICE.value)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().statuses(Collections.singletonList("Submitted")).build())
                .searchingData(null)
                .build();
        roles.add("ROLE_TRADEMARKS");
        Set<GrantedAuthority> authorities = new HashSet<>(AuthorityUtils.createAuthorityList("ROLE_TRADEMARKS"));
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Collections.singletonMap("name", USERNAME), "name");
        authentication = new OAuth2AuthenticationToken(oAuth2User, authorities, "userarea");
        mainAccount = DomainAccount.builder().username(USERNAME).firstName("Jack").surName("Cool").type(AccountType.PARENT.name()).build();
        noteApplication = NoteApplication.builder().applicationIdentifier(NUMBER).note(NOTE).build();
    }

    @Test
    public void testTrademarks() {
        when(accountService.isMainAccount(USERNAME)).thenReturn(true);
        when(applicationService.getApplications(applicationRequestTrademark, roles)).thenReturn(pageableApplicationTrademark);
        ResponseEntity<ApplicationSearchResultResource> result = applicationController.getApplications(TRADEMARK, applicationRequestResourceTrademark, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApplicationSearchResultResource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testTrademarksChildAccount() {
        when(accountService.isMainAccount(USERNAME)).thenReturn(false);
        when(accountService.getMainAccount(USERNAME)).thenReturn(mainAccount);
        when(applicationService.getApplications(applicationRequestTrademark, roles)).thenReturn(pageableApplicationTrademark);
        ResponseEntity<ApplicationSearchResultResource> result = applicationController.getApplications(TRADEMARK, applicationRequestResourceTrademark, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApplicationSearchResultResource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testDesigns() {
        when(applicationService.getApplications(applicationRequestDesign, roles)).thenReturn(pageableApplicationDesign);
        ResponseEntity<ApplicationSearchResultResource> result = applicationController.getApplications(DESIGN, applicationRequestResourceDesign, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApplicationSearchResultResource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testEservices() {
        when(applicationService.getApplications(applicationRequestEservice, roles)).thenReturn(pageableApplicationEservice);
        ResponseEntity<ApplicationSearchResultResource> result = applicationController.getApplications(ESERVICE, applicationRequestResourceEservice, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApplicationSearchResultResource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetApplicationReceipt() {
        when(applicationService.getReceipt(USERNAME, 1L)).thenReturn("e04fd020ea3a6910a2d808002b30309d");
        ResponseEntity<Receipt> result = applicationController.getReceipt(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS,"1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Receipt response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetReceiptNoApplicationExist() {
        when(applicationService.getReceipt(USERNAME, 1L)).thenReturn(null);
        ResponseEntity<Receipt> result = applicationController.getReceipt(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS,"1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Receipt response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testGetApplicationInvoice(){
        final String fileName = "invoice.pdf";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(applicationService.getInvoice(USERNAME,1L)).thenReturn(fileInfo);
        ResponseEntity<byte[]> result = applicationController.getApplicationInvoice(ESERVICE, IP_RIGHT_TYPE_TRADEMARKS, "1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        byte[] response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetApplicationInvoiceNotFound(){
        when(applicationService.getInvoice(USERNAME,1L)).thenReturn(null);
        ResponseEntity<byte[]> result = applicationController.getApplicationInvoice(ESERVICE, IP_RIGHT_TYPE_TRADEMARKS, "1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        byte[] response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testDeleteDraft() {
        when(applicationService.delete(USERNAME, 1L)).thenReturn(true);
        ResponseEntity<DraftResponse> result = applicationController.delete(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS,"1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        DraftResponse response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testDeleteDraftNull() {
        when(applicationService.delete(USERNAME, 1L)).thenReturn(false);
        ResponseEntity<DraftResponse> result = applicationController.delete(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS,"1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        DraftResponse response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testGetApplicationExcel() {
        final String fileName = "report.xlsx";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(accountService.isMainAccount(USERNAME)).thenReturn(true);
        when(excelService.generateExcel(applicationRequestEservice, roles)).thenReturn(fileInfo);
        ResponseEntity<Resource> result = applicationController.generateExcel(ESERVICE, applicationRequestResourceEservice, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Resource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetApplicationExcelChildAccount() {
        final String fileName = "report.xlsx";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(accountService.isMainAccount(USERNAME)).thenReturn(false);
        when(accountService.getMainAccount(USERNAME)).thenReturn(mainAccount);
        when(excelService.generateExcel(applicationRequestEservice, roles)).thenReturn(fileInfo);
        ResponseEntity<Resource> result = applicationController.generateExcel(ESERVICE, applicationRequestResourceEservice, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Resource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetApplicationExcelError() {
        when(excelService.generateExcel(applicationRequestEservice, roles)).thenReturn(null);
        ResponseEntity<Resource> result = applicationController.generateExcel(ESERVICE, applicationRequestResourceEservice, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Resource response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testGetApplicationPdf() {
        final String fileName = "report.pdf";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(accountService.isMainAccount(USERNAME)).thenReturn(true);
        when(pdfService.generatePdf(applicationRequestEservice, roles)).thenReturn(fileInfo);
        ResponseEntity<Resource> result = applicationController.generatePdf(ESERVICE, applicationRequestResourceEservice, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Resource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetApplicationPdfChildAccount() {
        final String fileName = "report.pdf";
        final byte[] content = {2, 6, -2, 1, 7};
        FileInfo fileInfo = FileInfo.builder().fileContent(content).fileName(fileName).build();
        when(accountService.isMainAccount(USERNAME)).thenReturn(false);
        when(accountService.getMainAccount(USERNAME)).thenReturn(mainAccount);
        when(pdfService.generatePdf(applicationRequestEservice, roles)).thenReturn(fileInfo);
        ResponseEntity<Resource> result = applicationController.generatePdf(ESERVICE, applicationRequestResourceEservice, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Resource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testGetApplicationPdfError() {
        ResponseEntity<Resource> result = applicationController.generatePdf(ESERVICE, applicationRequestResourceTrademark, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Resource response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testResumeNonExistingDraftApplication() {
        Long nonExistingApplicationId = 9999L;
        when(applicationService.getResumeDraftUrl(USERNAME, nonExistingApplicationId)).thenReturn(Optional.empty());
        ResponseEntity<ResumeDraftResponse> result = applicationController.resume(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, nonExistingApplicationId, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    public void testResumeExistingDraftApplicationReturnsRedirect() {
        Long existingApplicationId = 1111L;
        String draftApplicationNumber = "ES1231244241";
        String redirectUrl = String.format("%sapplications/resume?id=%s", frontOfficeUrl, draftApplicationNumber);
        when(applicationService.getResumeDraftUrl(USERNAME, existingApplicationId)).thenReturn(Optional.of(redirectUrl));
        ResponseEntity<ResumeDraftResponse> result = applicationController.resume(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, existingApplicationId, authentication);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(redirectUrl, Objects.requireNonNull(result.getBody()).getUrl());
    }

    @Test
    public void testDuplicateNonExistingDraftApplication() {
        Long nonExistingApplicationId = 9999L;
        when(applicationService.getDuplicateApplicationUrl(USERNAME, nonExistingApplicationId)).thenReturn(Optional.empty());
        ResponseEntity<DuplicateApplicationResponse> result = applicationController.duplicate(TRADEMARK, nonExistingApplicationId, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    public void testDuplicateExistingDraftApplicationReturnsRedirect() {
        Long existingApplicationId = 1111L;
        String duplicateUrl = "https://localhost/sp-ui-tmefiling/wizard.htm?duplicate_id=EFEM202100000000014";
        when(applicationService.getDuplicateApplicationUrl(USERNAME, existingApplicationId)).thenReturn(Optional.of(duplicateUrl));
        ResponseEntity<DuplicateApplicationResponse> result = applicationController.duplicate(TRADEMARK, existingApplicationId, authentication);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void testLockDraft() {
        when(applicationService.lock(USERNAME, 1L)).thenReturn(true);
        ResponseEntity<DraftResponse> result = applicationController.lock(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, "1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        DraftResponse response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testLockDraftNull() {
        when(applicationService.lock(USERNAME, 1L)).thenReturn(false);
        ResponseEntity<DraftResponse> result = applicationController.lock(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, "1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        DraftResponse response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testUnlockDraft() {
        when(applicationService.unlock(USERNAME, 1L)).thenReturn(true);
        ResponseEntity<DraftResponse> result = applicationController.unlock(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, "1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        DraftResponse response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testUnlockDraftNull() {
        when(applicationService.unlock(USERNAME, 1L)).thenReturn(false);
        ResponseEntity<DraftResponse> result = applicationController.unlock(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, "1", authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        DraftResponse response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testUpdateApplicationNote() {
        when(applicationService.updateNote(USERNAME, noteApplication)).thenReturn(NOTE);
        ResponseEntity<NoteApplication> result = applicationController.updateNote(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, noteApplication, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        NoteApplication response = result.getBody();
        assertNotNull(response);
        assertEquals(NOTE, response.getNote());
    }

    @Test
    public void testUpdateApplicationNoteNotExist() {
        when(applicationService.updateNote(USERNAME, noteApplication)).thenReturn(null);
        ResponseEntity<NoteApplication> result = applicationController.updateNote(TRADEMARK, IP_RIGHT_TYPE_TRADEMARKS, noteApplication, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        NoteApplication response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testValidateEServiceInitiationWhenApplicationIdsEmpty() {
        ValidateEServiceRequest validateEServiceRequest = ValidateEServiceRequest.builder().applicationIds(new HashSet<>()).build();
        when(applicationService.validateEService(authentication.getName(), validateEServiceRequest)).thenReturn(ValidateEServiceResponse.builder()
                .invalidApplicationIds(new HashSet<>()).status(ValidationStatusEServiceType.INVALID).message("eservice.initiate.validation.applicationIds.empty").build());
        ResponseEntity<ValidateEServiceResponse> response = applicationController.validateEservice(validateEServiceRequest, authentication);
        assertNotNull(response);
        ValidateEServiceResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ValidationStatusEServiceType.INVALID, Objects.requireNonNull(body).getStatus());
        assertEquals(0, Objects.requireNonNull(body).getInvalidApplicationIds().size());
        assertEquals("eservice.initiate.validation.applicationIds.empty", Objects.requireNonNull(body).getMessage());
    }

    @Test
    public void testValidateEServiceInitiationIsValid() {
        ValidateEServiceRequest validateEServiceRequest = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Arrays.asList(1L, 2L, 3L))).build();
        when(applicationService.validateEService(authentication.getName(), validateEServiceRequest)).thenReturn(ValidateEServiceResponse.builder()
                .invalidApplicationIds(new HashSet<>()).status(ValidationStatusEServiceType.VALID).message("eservice.initiate.validation.success").build());
        ResponseEntity<ValidateEServiceResponse> response = applicationController.validateEservice(validateEServiceRequest, authentication);
        assertNotNull(response);
        ValidateEServiceResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ValidationStatusEServiceType.VALID, Objects.requireNonNull(body).getStatus());
        assertEquals(0, Objects.requireNonNull(body).getInvalidApplicationIds().size());
        assertEquals("eservice.initiate.validation.success", Objects.requireNonNull(body).getMessage());
    }

}
