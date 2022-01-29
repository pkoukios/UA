/*
 * $Id:: ApplicationServiceImplTest.java 2021/03/02 02:09 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service;

import eu.euipo.etmdn.userarea.business.core.api.service.NoteService;
import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.business.event.AuditEventPublisher;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationStatus;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.FilteringData;
import eu.euipo.etmdn.userarea.common.domain.FilteringDate;
import eu.euipo.etmdn.userarea.common.domain.PaginationData;
import eu.euipo.etmdn.userarea.common.domain.SortingData;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceRequest;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidateEServiceResponse;
import eu.euipo.etmdn.userarea.common.domain.eservice.ValidationStatusEServiceType;
import eu.euipo.etmdn.userarea.common.domain.exception.EntityNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.ServiceUnavailableException;
import eu.euipo.etmdn.userarea.common.domain.exception.ValidateEserviceException;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.MainAccount;
import eu.euipo.etmdn.userarea.common.persistence.entity.QualifiedService;
import eu.euipo.etmdn.userarea.common.persistence.repository.ApplicationRepository;
import eu.euipo.etmdn.userarea.common.persistence.repository.QualifiedServiceRepository;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.application.ApplicationSearchResult;
import eu.euipo.etmdn.userarea.domain.note.NoteApplication;
import eu.euipo.etmdn.userarea.persistence.entity.note.NoteApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private QualifiedServiceRepository qualifiedServiceRepository;
    @Mock
    private NoteService noteService;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private IpoConfiguration ipoConfiguration;
    @Mock
    private AccountService accountService;
    @Mock
    private Environment env;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private AuditEventPublisher auditEventPublisher;
    @Mock
    private PdfService pdfService;
    @Mock
    private PaymentService paymentService;

    private Page<Application> pageableApplicationTrademark;
    private ApplicationRequest applicationRequestTrademark;
    private ApplicationRequest applicationRequestDesign;
    private ApplicationRequest applicationRequestEservice;
    private Application applicationSignatureTrademark;
    private Application applicationSignatureDesign;
    private NoteApplication noteApplication;
    private final Set<String> roles = new HashSet<>();

    private static final String USERNAME = "abc@xyz.com";
    private static final String NOTE = "Test note";
    private static final String NUMBER = "12345678";

    @Before
    public void setUp() {
        applicationService = new ApplicationServiceImpl(applicationRepository, accountService, qualifiedServiceRepository, noteService, applicationConfiguration, ipoConfiguration, env, restTemplate, auditEventPublisher,paymentService,pdfService, "", "", "","","");
        Application applicationTrademark = Application.builder().number(NUMBER).foModule("Trademark").type("Word")
                .kind("Individual").status("Submitted").build();
        pageableApplicationTrademark = new PageImpl<>(Collections.singletonList(applicationTrademark));
        applicationRequestTrademark = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.TRADEMARK.value)
                .isDraft(false)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().build())
                .searchingData(null)
                .build();
        applicationRequestDesign = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.DESIGN.value)
                .isDraft(false)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().build())
                .searchingData(null)
                .build();
        applicationRequestEservice = ApplicationRequest.builder()
                .userName(USERNAME)
                .applicationType(ApplicationType.ESERVICE.value)
                .isDraft(false)
                .paginationData(PaginationData.builder().page(0).size(10).build())
                .sortingData(SortingData.builder().sortColumn("number").sortType("DESC").build())
                .filteringData(FilteringData.builder().build())
                .searchingData(null)
                .build();
        applicationSignatureTrademark = Application.builder().number(NUMBER).foModule("trademark").type("Word")
                .kind("Individual").status("Pending Signature").build();
        applicationSignatureDesign = Application.builder().number("9912345678").foModule("design").status("Pending Signature").build();
        ApplicationStatus applicationStatus = ApplicationStatus.builder()
                .application("Expired,Registered,Ended")
                .draft("Initialized")
                .signature("Pending Signature")
                .payment("Pending Signature")
                .build();

        when(applicationConfiguration.getStatus()).thenReturn(applicationStatus);
        when(noteService.getNoteByApplicationNumber(any())).thenReturn(NoteApplicationEntity.builder().note("test").build());
        when(noteService.getNoteByApplicationNumberAndLock(any(), any())).thenReturn(NoteApplicationEntity.builder().note("test").build());
        noteApplication = NoteApplication.builder().applicationIdentifier(NUMBER).note(NOTE).build();
    }

    @Test(expected = ServiceUnavailableException.class)
    public void testGetServiceUnavailableException() {
        ApplicationSearchResult result = new ApplicationSearchResult();
        result.setContent(Collections.singletonList(ApplicationDetails.builder().number(NUMBER).build()));
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenThrow(new ServiceUnavailableException(""));
        applicationService.getApplications(applicationRequestTrademark, roles);
    }

    @Test
    public void testGetTrademarksWithoutFilters() {
        ApplicationSearchResult result = new ApplicationSearchResult();
        result.setContent(Collections.singletonList(ApplicationDetails.builder().number(NUMBER).build()));
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetTrademarksFilteringType() {
        applicationRequestTrademark.getFilteringData().setTypes(Arrays.asList("Word", "Sound"));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetTrademarksFilteringKind() {
        applicationRequestTrademark.getFilteringData().setKinds(Arrays.asList("Individual", "Collective"));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetTrademarksFilteringStatus() {
        applicationRequestTrademark.getFilteringData().setStatuses(Arrays.asList("Submitted", "Expired"));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetTrademarksFilteringClasses() {
        applicationRequestTrademark.getFilteringData().setClasses(Arrays.asList("1", "2", "3"));
        ApplicationSearchResult result = new ApplicationSearchResult();

        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDraftTrademarksFilteringClasses() {
        applicationRequestTrademark.setIsDraft(true);
        applicationRequestTrademark.getFilteringData().setClasses(Arrays.asList("1", "2", "3"));
        applicationRequestTrademark.getFilteringData().setDates(Collections.singletonList(
                FilteringDate.builder().date("creationDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build()));
        applicationRequestTrademark.setSearchingData("5678");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetTrademarksFilteringDates() {
        applicationRequestTrademark.getFilteringData().setDates(Arrays.asList(
                FilteringDate.builder().date("applicationDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build(),
                FilteringDate.builder().date("statusDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build()));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetTrademarksSearching() {
        applicationRequestTrademark.setSearchingData("5678");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestTrademark, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDesignsWithoutFilters() {

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestDesign, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDesignsFilteringStatus() {
        applicationRequestDesign.getFilteringData().setStatuses(Arrays.asList("Word", "Sound"));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestDesign, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDesignsFilteringLocarno() {
        applicationRequestDesign.getFilteringData().setLocarno("01");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestDesign, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDesignsFilteringDates() {
        applicationRequestDesign.getFilteringData().setDates(Arrays.asList(
                FilteringDate.builder().date("applicationDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build(),
                FilteringDate.builder().date("statusDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build()));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestDesign, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDesignsSearching() {
        applicationRequestDesign.setSearchingData("5678");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestDesign, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetDraftDesignsFilteringClasses() {
        applicationRequestDesign.setIsDraft(true);
        applicationRequestDesign.getFilteringData().setDates(Collections.singletonList(
                FilteringDate.builder().date("creationDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build()));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestDesign, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceFilteringType() {
        applicationRequestEservice.getFilteringData().setFormTypes(Arrays.asList("TM_TRANSFER", "TM_LIMITATION"));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceFilteringStatus() {
        applicationRequestEservice.getFilteringData().setStatuses(Arrays.asList("Submitted", "Expired"));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceFilteringIpRightType() {
        roles.add("ROLE_TRADEMARKS");
        roles.add("ROLE_DESIGNS");
        applicationRequestEservice.getFilteringData().setIpRightType("Trademarks");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceOnlyTrademarkRole() {
        roles.add("ROLE_TRADEMARKS");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceOnlyDesignRole() {
        roles.add("ROLE_DESIGNS");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceFilteringDates() {
        applicationRequestEservice.getFilteringData().setDates(Arrays.asList(
                FilteringDate.builder().date("applicationDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build(),
                FilteringDate.builder().date("statusDate").dateFrom(LocalDateTime.now().minusMonths(6L)).dateTo(LocalDateTime.now()).build()));

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }

    @Test
    public void testGetEserviceSearching() {
        applicationRequestEservice.setSearchingData("5678");

        ApplicationSearchResult result = new ApplicationSearchResult();
        when(restTemplate.postForObject(any(String.class),any(Object.class),any(Class.class),any(Object.class))).thenReturn(result);
        ApplicationSearchResult applicationSearchResult = applicationService.getApplications(applicationRequestEservice, roles);
        assertNotNull(applicationSearchResult);
    }


    @Test
    public void testDeleteDraft() {
        Application draft = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").lockedBy(USERNAME).mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(draft);
        when(applicationRepository.getApplicationById(1L)).thenReturn(draft);
        when(applicationRepository.findByNumber(draft.getNumber())).thenReturn(Collections.singletonList(draft));
        when(applicationRepository.save(any())).thenReturn(draft);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        boolean isDeleted = applicationService.delete(USERNAME, 1L);
        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteDraftNull() {
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(null);
        boolean isDeleted = applicationService.delete(USERNAME, 1L);
        Assert.assertFalse(isDeleted);
    }

    @Test
    public void testDeleteLockedApplication() {
        boolean isDeleted = applicationService.delete(USERNAME, 1L);
        Assert.assertFalse(isDeleted);
    }

    @Test
    public void testLockDraft() {
        Application draft = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(draft);
        boolean isLocked = applicationService.lock(USERNAME, 1L);
        Assert.assertTrue(isLocked);
    }

    @Test
    public void testLockDraftNull() {
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(null);
        boolean isLocked = applicationService.lock(USERNAME, 1L);
        Assert.assertFalse(isLocked);
    }

    @Test
    public void testUnlockDraft() {
        Application draft = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(draft);
        boolean isLocked = applicationService.unlock(USERNAME, 1L);
        Assert.assertTrue(isLocked);
    }

    @Test
    public void testUnlockDraftNull() {
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(null);
        boolean isLocked = applicationService.unlock(USERNAME, 1L);
        Assert.assertFalse(isLocked);
    }

    @Test
    public void testGetApplicationReceipt() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Submitted").mainAccount(MainAccount.builder().username(USERNAME).build())
                .receipt("e04fd020ea3a6910a2d808002b30309d").build();
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        String receipt = applicationService.getReceipt(USERNAME, 1L);
        assertNotNull(receipt);
    }

    @Test
    public void testGetApplicationReceiptNull() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Submitted").mainAccount(MainAccount.builder().username(USERNAME).build()).receipt(null).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        String receipt = applicationService.getReceipt(USERNAME, 1L);
        Assert.assertNull(receipt);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotReturnDraftResumeUrlWhenEntityNotFound() {
        when(applicationRepository.findByIdAndStatusIsIn(anyLong(), anyList())).thenReturn(null);
        applicationService.getResumeDraftUrl(USERNAME, 1L);
    }

    @Test
    public void testGetTrademarkDraftResumeUrl() {
        Application application = Application.builder().id(1L).number("12345678").foModule("trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(application);
        when(applicationRepository.getApplicationById(1L)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);
        when(applicationService.getByIdAndLock(1L,USERNAME)).thenReturn(application);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        Optional<String> resumeDraftUrl = applicationService.getResumeDraftUrl(USERNAME, 1L);
        assertNotNull(resumeDraftUrl);
    }

    @Test
    public void testGetDesignDraftResumeUrl() {
        Application application = Application.builder().id(1L).number("12345678").foModule("design").status("Initialized")
                .mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.getApplicationById(1L)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);
        when(applicationService.getByIdAndLock(1L,USERNAME)).thenReturn(application);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(application);
        Optional<String> resumeDraftUrl = applicationService.getResumeDraftUrl(USERNAME, 1L);
        assertNotNull(resumeDraftUrl);
    }

    @Test
    public void testGetEserviceDraftResumeUrl() {
        Application application = Application.builder().id(1L).number("12345678").foModule("eservice").eserviceCode("TM_TRANSFER")
                .status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.getApplicationById(1L)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);
        when(applicationService.getByIdAndLock(1L,USERNAME)).thenReturn(application);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(application);
        Optional<String> resumeDraftUrl = applicationService.getResumeDraftUrl(USERNAME, 1L);
        assertNotNull(resumeDraftUrl);
    }

    @Test
    public void testGetEserviceDraftResumeUrlNotAllowed() {
        Application application = Application.builder().id(1L).number("12345678").foModule("eservice").eserviceCode("TM_TRANSFER")
                .status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.getApplicationById(1L)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);
        when(applicationService.getByIdAndLock(1L,USERNAME)).thenReturn(application);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(false);
        when(applicationRepository.findByIdAndStatusIsIn(1L, Collections.singletonList("Initialized"))).thenReturn(application);
        Optional<String> resumeDraftUrl = applicationService.getResumeDraftUrl(USERNAME, 1L);
        assertNotNull(resumeDraftUrl);
    }

    @Test
    public void testGetApplicationByModuleAndStatus() {
        when(applicationRepository.findByFoModuleAndStatusIsIn(any(String.class), any(List.class), any(Pageable.class))).thenReturn(pageableApplicationTrademark);
        Page<Application> trademarks = applicationService.getApplicationsByFoModuleAndStatus(applicationRequestTrademark, ApplicationType.TRADEMARK, Collections.singletonList("Submitted"));
        assertNotNull(trademarks);
    }

    @Test
    public void testSaveAllApplications() {
        applicationService.saveAll(Collections.singletonList(pageableApplicationTrademark.getContent().get(0)));
        verify(applicationRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    public void testLockApplicationNote() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        String note = applicationService.lockNote(USERNAME, noteApplication);
        assertNotNull(note);
    }

    @Test
    public void testUpdateApplicationNote() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        String note = applicationService.updateNote(USERNAME, noteApplication);
        assertNotNull(note);
    }

    @Test
    public void testAddApplicationNote() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(noteService.getNoteByApplicationNumber(application.getNumber())).thenReturn(null);
        String note = applicationService.updateNote(USERNAME, noteApplication);
        assertNotNull(note);
    }

    @Test
    public void testDuplicateTrademarkApplicationUrl() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        Optional<String> duplicateApplicationUrl = applicationService.getDuplicateApplicationUrl(USERNAME, 1L);
        assertNotNull(duplicateApplicationUrl);
        Assert.assertTrue(duplicateApplicationUrl.isPresent());
    }

    @Test
    public void testDuplicateDraftDesignApplicationUrl() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Design")
                .status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        Optional<String> duplicateApplicationUrl = applicationService.getDuplicateApplicationUrl(USERNAME, 1L);
        assertNotNull(duplicateApplicationUrl);
        Assert.assertTrue(duplicateApplicationUrl.isPresent());
    }

    @Test
    public void testValidateEServiceTMWithdrawalMultipleAnyStatusIsValid() {
        // Setup
        Application tmWithdrawal1 = Application.builder()
                .id(1L)
                .status("Registered").applicationDate(LocalDateTime.now()).number("NUMBER1").build();
        Application tmWithdrawal2 = Application.builder()
                .id(2L)
                .status("Submitted").applicationDate(LocalDateTime.now()).number("NUMBER2").build();

        ValidateEServiceRequest validateEServiceRequest = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Arrays.asList(1L, 2L))).build();

        when(applicationRepository.findAllById(any())).thenReturn(Arrays.asList(tmWithdrawal1, tmWithdrawal2));
        when(qualifiedServiceRepository.findFirstByService(validateEServiceRequest.getEserviceType())).thenReturn(
                QualifiedService.builder().id(1L).multiplicity("MULTIPLE").eligibility("ALL").dataRange("ANYTIME").service("TM Withdrawal/Surrender").build()
        );
        // Exercise
        ValidateEServiceResponse responseBody = applicationService.validateEService(USERNAME, validateEServiceRequest);

        // Verify
        assertNotNull(responseBody);
        Assert.assertEquals(ValidationStatusEServiceType.VALID, responseBody.getStatus());
        Assert.assertEquals("eservice.initiate.validation.valid", responseBody.getMessage());
        Assert.assertEquals(0, responseBody.getInvalidApplicationIds().size());
        Assert.assertEquals(0, responseBody.getInvalidApplicationNumbers().size());
    }

    @Test(expected = ValidateEserviceException.class)
    public void testValidateEServiceTMWithdrawalMultipleThrowsException() {
        Application tmWithdrawal1 = Application.builder()
                .id(1L)
                .status("Registered").applicationDate(LocalDateTime.now()).number("NUMBER1").build();
        Application tmWithdrawal2 = Application.builder()
                .id(2L)
                .status("Submitted").applicationDate(LocalDateTime.now()).number("NUMBER2").build();

        ValidateEServiceRequest validateEServiceRequest = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Arrays.asList(1L, 2L))).build();

        when(applicationRepository.findAllById(any())).thenReturn(Arrays.asList(tmWithdrawal1, tmWithdrawal2));
        when(qualifiedServiceRepository.findFirstByService(validateEServiceRequest.getEserviceType())).thenReturn(null);
        applicationService.validateEService(USERNAME, validateEServiceRequest);
    }

    @Test
    public void testTMWithdrawalOneIsValid() {
        // Setup
        Application tmWithdrawal1 = Application.builder()
                .id(1L)
                .status("Registered")
                .applicationDate(LocalDateTime.now())
                .number("NUMBER1").build();

        ValidateEServiceRequest request = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Collections.singletonList(1L))).build();

        ValidateEServiceRequest validateEServiceRequest = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Collections.singletonList(1L))).build();

        when(applicationRepository.findAllById(any())).thenReturn(Collections.singletonList(tmWithdrawal1));
        when(qualifiedServiceRepository.findFirstByService(validateEServiceRequest.getEserviceType())).thenReturn(
                QualifiedService.builder().id(1L).multiplicity("MULTIPLE").eligibility("ALL").dataRange("ANYTIME").service("TM Withdrawal/Surrender").build()
        );
        // Exercise
        ValidateEServiceResponse responseBody = applicationService.validateEService(USERNAME, request);

        // Verify
        Assert.assertEquals(ValidationStatusEServiceType.VALID, responseBody.getStatus());
        Assert.assertEquals("eservice.initiate.validation.valid", responseBody.getMessage());
        Assert.assertTrue(responseBody.getInvalidApplicationIds().isEmpty());
        Assert.assertTrue(responseBody.getInvalidApplicationNumbers().isEmpty());
    }

    @Test
    public void testTMGenericMultipleIsValid() {
        // Setup
        Application tmWithdrawal1 = Application.builder()
                .id(1L)
                .status("Registered")
                .foModule(ApplicationType.TRADEMARK.value)
                .applicationDate(LocalDateTime.now())
                .number("NUMBER1").build();

        ValidateEServiceRequest request = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Collections.singletonList(1L)))
                .build();

        when(applicationRepository.findAllById(any())).thenReturn(Collections.singletonList(tmWithdrawal1));
        when(qualifiedServiceRepository.findFirstByService(request.getEserviceType())).thenReturn(
                QualifiedService.builder().id(1L).multiplicity("MULTIPLE").eligibility("ALL").dataRange("ANYTIME").service("TM Withdrawal/Surrender").build()
        );
        // Exercise
        ValidateEServiceResponse responseBody = applicationService.validateEService(USERNAME, request);

        // Verify
        Assert.assertEquals(ValidationStatusEServiceType.VALID, responseBody.getStatus());
        Assert.assertEquals("eservice.initiate.validation.valid", responseBody.getMessage());
        Assert.assertTrue(responseBody.getInvalidApplicationIds().isEmpty());
        Assert.assertTrue(responseBody.getInvalidApplicationNumbers().isEmpty());
    }

    @Test
    public void testTMGenericMultipleIsInValid() {
        Application tmWithdrawal1 = Application.builder()
                .id(1L)
                .status("Registered")
                .foModule(ApplicationType.TRADEMARK.value)
                .applicationDate(LocalDateTime.now())
                .number("NUMBER1").build();
        ValidateEServiceRequest request = ValidateEServiceRequest.builder()
                .applicationIds(new HashSet<>(Collections.singletonList(1L)))
                .build();
        when(applicationRepository.findAllById(any())).thenReturn(Collections.singletonList(tmWithdrawal1));
        when(qualifiedServiceRepository.findFirstByService(request.getEserviceType())).thenReturn(
                QualifiedService.builder().id(1L).multiplicity("MULTIPLE").eligibility("ONE").dataRange("ANYTIME").service("TM Withdrawal/Surrender").build()
        );
        ValidateEServiceResponse responseBody = applicationService.validateEService(USERNAME, request);

        Assert.assertEquals(ValidationStatusEServiceType.INVALID, responseBody.getStatus());
        Assert.assertEquals("eservice.initiate.validation.fail", responseBody.getMessage());
        Assert.assertFalse(responseBody.getInvalidApplicationIds().isEmpty());
        Assert.assertFalse(responseBody.getInvalidApplicationNumbers().isEmpty());
    }

    @Test
    public void testRightInRemMultipleRegisteredIsInvalid() {
        // Setup
        Application tmWithdrawal1 = Application.builder()
                .id(1L)
                .status("Registered")
                .foModule(ApplicationType.TRADEMARK.value)
                .applicationDate(LocalDateTime.now())
                .number("NUMBER1").build();

        Application tmWithdrawal2 = Application.builder()
                .id(2L)
                .status("Registered")
                .foModule(ApplicationType.TRADEMARK.value)
                .applicationDate(LocalDateTime.now())
                .number("NUMBER1").build();

        ValidateEServiceRequest request = ValidateEServiceRequest.builder()
                .eserviceType("TM Right in rem")
                .applicationIds(new HashSet<>(Arrays.asList(1L, 2L)))
                .build();

        // Exercise
        when(applicationRepository.findAllById(any())).thenReturn(Arrays.asList(tmWithdrawal1, tmWithdrawal2));
        when(qualifiedServiceRepository.findFirstByService(request.getEserviceType())).thenReturn(
                QualifiedService.builder()
                        .id(1L)
                        .multiplicity("ONE")
                        .eligibility("REGISTERED").dataRange("ANYTIME")
                        .service("TM Right in rem").build()
        );

        // Exercise
        ValidateEServiceResponse responseBody = applicationService.validateEService(USERNAME, request);

        // Verify
        assertNotNull(responseBody);
        Assert.assertEquals(ValidationStatusEServiceType.INVALID, responseBody.getStatus());
        Assert.assertEquals("eservice.initiate.validation.multiplicity.fail", responseBody.getMessage());
        Assert.assertTrue(responseBody.isMultiplicityError());
        Assert.assertFalse(responseBody.getInvalidApplicationIds().isEmpty());
        Assert.assertFalse(responseBody.getInvalidApplicationNumbers().isEmpty());
    }

    @Test
    public void testGetApplicationForSignatureTrademark() {
        roles.add("ROLE_TRADEMARKS");
        when(accountService.getMainAccount(USERNAME)).thenReturn(DomainAccount.builder().username(USERNAME).build());
        when(applicationRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(applicationSignatureTrademark));
        List<Application> trademarks = applicationService.getApplicationsForSignatures(USERNAME, roles);
        assertNotNull(trademarks);
    }

    @Test
    public void testGetApplicationForSignatureDesign() {
        roles.add("ROLE_DESIGNS");
        when(accountService.getMainAccount(USERNAME)).thenReturn(DomainAccount.builder().username(USERNAME).build());
        when(applicationRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(applicationSignatureDesign));
        List<Application> designs = applicationService.getApplicationsForSignatures(USERNAME, roles);
        assertNotNull(designs);
    }

    @Test
    public void testGetApplicationForSignatureTrademarkAndDesign() {
        roles.add("ROLE_TRADEMARKS");
        roles.add("ROLE_DESIGNS");
        when(accountService.getMainAccount(USERNAME)).thenReturn(DomainAccount.builder().username(USERNAME).build());
        when(applicationRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(applicationSignatureTrademark, applicationSignatureDesign));
        List<Application> applications = applicationService.getApplicationsForSignatures(USERNAME, roles);
        assertNotNull(applications);
    }

    @Test
    public void testGetInvoice(){
        PaymentApplicationEntity paymentApplication = new PaymentApplicationEntity();
        PaymentEntity payment = new PaymentEntity();
        payment.setTransactionId("transactionId");
        paymentApplication.setPayment(payment);
        when(paymentService.getPaymentApplicationByApplicationId(1L)).thenReturn(paymentApplication);
        when(pdfService.generatePdf(USERNAME,"transactionId")).thenReturn(new FileInfo());
        assertNotNull(applicationService.getInvoice(USERNAME,1L));
    }

    @Test
    public void testGetInvoicePaymentApplicationNotFound(){
        when(paymentService.getPaymentApplicationByApplicationId(1L)).thenReturn(null);
        Assert.assertNull(applicationService.getInvoice(USERNAME,1L));
    }

    @Test
    public void testGetInvoicePaymentTransactionIsEmpty(){
        PaymentApplicationEntity paymentApplication = new PaymentApplicationEntity();
        PaymentEntity payment = new PaymentEntity();
        paymentApplication.setPayment(payment);
        when(paymentService.getPaymentApplicationByApplicationId(1L)).thenReturn(paymentApplication);
        Assert.assertNull(applicationService.getInvoice(USERNAME,1L));
    }


    @Test(expected = EntityNotFoundException.class)
    public void testDeleteApplicationFromSignature() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").lockedBy(USERNAME).mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.EMPTY_LIST);
        applicationService.deleteApplication(USERNAME, application.getNumber());
    }

    @Test
    public void testDeleteApplicationFromSignatureWithoutLock() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.singletonList(application));
        when(applicationRepository.save(application)).thenReturn(application);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        applicationService.deleteApplication(USERNAME, application.getNumber());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteApplicationFromSignatureNotValidApplication() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").lockedBy("test").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.EMPTY_LIST);
        applicationService.deleteApplication(USERNAME, application.getNumber());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testModifyApplicationFromSignature() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").lockedBy("test").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.EMPTY_LIST);
        String resume = applicationService.modifyApplication(USERNAME, application.getNumber());
        assertNotNull(resume);
    }

    @Test
    public void testModifyApplicationFromSignatureWithoutLock() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.singletonList(application));
        when(applicationRepository.save(application)).thenReturn(application);
        when(accountService.isAllowedToModifyApplication(USERNAME, USERNAME)).thenReturn(true);
        String resume = applicationService.modifyApplication(USERNAME, application.getNumber());
        assertNotNull(resume);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testModifyApplicationFromSignatureNotValidApplication() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").lockedBy(USERNAME).mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.EMPTY_LIST);
        String resume = applicationService.modifyApplication(USERNAME, application.getNumber());
        Assert.assertNull(resume);
    }

    @Test
    public void testFindByApplicationNumbers() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumber(application.getNumber())).thenReturn(Collections.singletonList(application));
        List<Application> applicationList= applicationService.findByNumber(application.getNumber());
        assertNotNull(applicationList);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testFindByApplicationNumbersNotValid() {
        when(applicationRepository.findByNumber("12345678")).thenReturn(null);
        List<Application> applicationList= applicationService.findByNumber("12345678");
        Assert.assertNull(applicationList);
    }

    @Test
    public void testGetApplicationsByApplicationIds() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByIdIsIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(application));
        List<Application> applicationList= applicationService.getApplicationsByIds(Collections.singletonList(1L));
        assertNotNull(applicationList);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetApplicationsByApplicationIdsNotValid() {
        when(applicationRepository.findByIdIsIn(Collections.singletonList(1L))).thenReturn(null);
        List<Application> applicationList= applicationService.getApplicationsByIds(Collections.singletonList(1L));
        Assert.assertNull(applicationList);
    }

    @Test
    public void testGetApplicationsByNumber() {
        Application application = Application.builder().id(1L).number("12345678").foModule("Trademark").type("Word")
                .kind("Individual").status("Initialized").mainAccount(MainAccount.builder().username(USERNAME).build()).build();
        when(applicationRepository.findByNumberIsIn(Collections.singletonList("12345678"))).thenReturn(Collections.singletonList(application));
        List<Application> applicationList= applicationService.getApplicationsByNumber(Collections.singletonList("12345678"));
        assertNotNull(applicationList);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetApplicationsByNumberNotValid() {
        when(applicationRepository.findByNumberIsIn(Collections.singletonList("12345678"))).thenReturn(null);
        List<Application> applicationList= applicationService.getApplicationsByNumber(Collections.singletonList("12345678"));
        Assert.assertNull(applicationList);
    }
}