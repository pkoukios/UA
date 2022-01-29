/*
 * $Id:: UserProfileServiceImpl.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.domain.ApplicationSignature;
import eu.euipo.etmdn.userarea.common.domain.ApplicationStatus;
import eu.euipo.etmdn.userarea.common.domain.SignatureStatus;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.exception.SignatureClientException;
import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.signature.SignatureEntity;
import eu.euipo.etmdn.userarea.common.persistence.repository.SignatureRepository;
import eu.euipo.etmdn.userarea.domain.application.ApplicationDetails;
import eu.euipo.etmdn.userarea.domain.signature.PlatformSignatureDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;
import eu.euipo.etmdn.userarea.domain.signature.SignResource;
import eu.euipo.etmdn.userarea.domain.signature.SignatoryDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortColumn;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortingCriteriaRequest;
import eu.euipo.etmdn.userarea.external.signature.api.client.SignatureClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SignatureServiceImplTest {

    @InjectMocks
    private SignatureServiceImpl signatureService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    private SignatureClient signatureClient;

    @Mock
    private SignatureRepository signatureRepository;

    @Mock
    private ShoppingCartService shoppingCartService;

    @Mock
    private RestTemplate restTemplate;

    private Application applicationTrademark;
    private SignatureSortingCriteriaRequest signatureSortingCriteriaRequest;
    private final Set<String> roles = new HashSet<>();
    private static final String USERNAME = "abc@xyz.com";
    private static final String NUMBER = "12345";

    @Before
    public void setUp() {
        signatureSortingCriteriaRequest = new SignatureSortingCriteriaRequest();
        signatureService = new SignatureServiceImpl(applicationService, applicationConfiguration, signatureClient, signatureRepository,
                shoppingCartService, restTemplate, "", "", "", "", "");

        ApplicationSignature applicationSignature = ApplicationSignature.builder()
                .maxSignatories(5)
                .status(SignatureStatus.builder().completed("COMPLETED").build())
                .build();
        when(applicationConfiguration.getSignature()).thenReturn(applicationSignature);
        SignatureEntity signatureTrademark = SignatureEntity.builder().id(1L)
                .name("Name").capacity("Capacity").date(LocalDateTime.now()).status("COMPLETED").build();
        applicationTrademark = Application.builder().number("12345678").foModule("trademark").type("Word")
                .kind("Individual").status("Pending Signature")
                .signatures(new ArrayList<>(Arrays.asList(signatureTrademark))).build();
        roles.addAll(Arrays.asList("ROLE_TRADEMARKS", "ROLE_SIGNATURES"));
        ApplicationStatus applicationStatus = ApplicationStatus.builder()
                .signature("Pending Signature")
                .payment("Pending Signature")
                .build();
        when(applicationConfiguration.getStatus()).thenReturn(applicationStatus);
    }

    @Test
    public void testTrademarkApplicationWithSignatures() {
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());
    }

    @Test
    public void testTrademarkApplicationWithSignaturesNotValidUser() {
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(null);
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertEquals(0, signatures.size());
    }

    @Test
    public void testSortAscendingSignatureSortColumnType() {
        signatureSortingCriteriaRequest.setSortType("ASC");
        signatureSortingCriteriaRequest.setSortColumn(SignatureSortColumn.TYPE);
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());
    }

    @Test
    public void testSortAscendingSignatureSortColumnApplication() {
        signatureSortingCriteriaRequest.setSortType("ASC");
        signatureSortingCriteriaRequest.setSortColumn(SignatureSortColumn.APPLICATION);
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());
    }

    @Test
    public void testSortAscendingSignatureSortColumnSignedat() {
        signatureSortingCriteriaRequest.setSortType("ASC");
        signatureSortingCriteriaRequest.setSortColumn(SignatureSortColumn.SIGNEDAT);
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());
    }

    @Test
    public void testSortDescendingSignatureSortColumnType() {
        signatureSortingCriteriaRequest.setSortType("DESC");
        signatureSortingCriteriaRequest.setSortColumn(SignatureSortColumn.TYPE);
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());

    }

    @Test
    public void testSortDescendingSignatureSortColumnApplication() {
        signatureSortingCriteriaRequest.setSortType("DESC");
        signatureSortingCriteriaRequest.setSortColumn(SignatureSortColumn.APPLICATION);
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());
    }

    @Test
    public void testSortDescendingSignatureSortColumnSignedDate() {
        signatureSortingCriteriaRequest.setSortType("DESC");
        signatureSortingCriteriaRequest.setSortColumn(SignatureSortColumn.SIGNEDAT);
        when(applicationService.getApplicationsForSignatures(USERNAME, roles)).thenReturn(Collections.singletonList(applicationTrademark));
        List<Signature> signatures = signatureService.getSignatures(USERNAME, roles, signatureSortingCriteriaRequest);
        assertNotNull(signatures);
        assertFalse(signatures.isEmpty());
    }

    @Test
    public void testDeleteApplicationFromSignatures() {
        doNothing().when(applicationService).deleteApplication(USERNAME, NUMBER);
        signatureService.deleteApplication(USERNAME, NUMBER);
    }

    @Test
    public void testModifyApplicationFromSignatures() {
        when(applicationService.modifyApplication(USERNAME, NUMBER)).thenReturn("resumeUrl");
        String resumeUrl = signatureService.modifyApplication(USERNAME, NUMBER);
        assertNotNull(resumeUrl);
    }

    @Test
    public void testModifyApplicationFromSignaturesNotValidApplication() {
        when(applicationService.modifyApplication(USERNAME, NUMBER)).thenReturn(null);
        String resumeUrl = signatureService.modifyApplication(USERNAME, NUMBER);
        assertNull(resumeUrl);
    }

    @Test
    public void testSignRequestWith2Applications() {
        Set<String> applicationIds = new HashSet<>();
        applicationIds.add("EFR123131231");
        applicationIds.add("EFR313241432");

        SignRequest signRequest = SignRequest.builder()
                .signatoryDetails(
                        SignatoryDetails.builder()
                                .capacity("Applicant")
                                .email("test@test.com")
                                .fullName("Test Testopoulos")
                                .build()
                )
                .applicationIds(applicationIds).build();

        when(signatureClient.createSignatureId(signRequest)).thenReturn("signatureId");

        assertNotNull(signatureService.createSignatureId(signRequest));
    }

    @Test
    public void testSignRequestWithNoApplications() {
        Set<String> applicationIds = new HashSet<>();

        SignRequest signRequest = SignRequest.builder()
                .signatoryDetails(
                        SignatoryDetails.builder()
                                .capacity("Applicant")
                                .email("test@test.com")
                                .fullName("Test Testopoulos")
                                .build()
                )
                .applicationIds(applicationIds).build();

        when(signatureClient.createSignatureId(signRequest)).thenReturn("signatureId");

        assertNotNull(signatureService.createSignatureId(signRequest));
    }

    @Test
    public void testProcessSignature(){
        Set<String> applicationIds = new HashSet<>();
        applicationIds.add("EFR123131231");
        SignRequest signRequest = SignRequest.builder()
                .signatoryDetails(
                        SignatoryDetails.builder()
                                .capacity("Applicant")
                                .email("test@test.com")
                                .fullName("Test Testopoulos")
                                .build()
                ).applicationIds(applicationIds).build();

        when(signatureClient.createSignatureId(signRequest)).thenReturn("signatureId");
        when(applicationService.getApplicationsByNumber(new ArrayList<>(signRequest.getApplicationIds())))
                .thenReturn(Collections.singletonList(applicationTrademark));
        when(applicationService.getByIdAndLock(applicationTrademark.getId(),USERNAME)).thenReturn(applicationTrademark);
        SignResource signResource = signatureService.processSignature(USERNAME,signRequest);
        assertNotNull(signResource);
    }

    @Test
    public void testGetAllApplicationDetailsBy(){
        when(signatureRepository.findAllByReferenceAndDeleted("signatureId",false))
                .thenReturn(applicationTrademark.getSignatures());
        List<ApplicationDetails> applicationDetails = signatureService.getAllApplicationDetailsBy("signatureId");
        assertFalse(applicationDetails.isEmpty());
    }

    @Test(expected = SignatureClientException.class)
    public void testCreateSignatureIdWhenSignatureIdIsNull(){
        SignRequest signRequest = SignRequest.builder()
                .signatoryDetails(
                        SignatoryDetails.builder()
                                .capacity("Applicant")
                                .email("test@test.com")
                                .fullName("Test Testopoulos")
                                .build()
                ).build();
        when(signatureClient.createSignatureId(signRequest)).thenReturn(null);
        signatureService.createSignatureId(signRequest);
    }

    @Test
    public void getNonFullySignedApplicationDetailsBySignatureId() {
        applicationTrademark.getSignatures().get(0).setApplication(applicationTrademark);
        when(signatureRepository.findAllByReferenceAndDeleted("signatureId",false))
                .thenReturn(applicationTrademark.getSignatures());
        when(signatureRepository.findAllByApplicationIdAndStatusAndDeleted(applicationTrademark.getId(),
                applicationConfiguration.getSignature().getStatus().getCompleted(), false))
                .thenReturn(applicationTrademark.getSignatures());
        List<String> list = signatureService.getNonFullySignedApplicationDetailsBySignatureId(USERNAME,"signatureId");
        assertFalse(list.isEmpty());
    }

    @Test
    public void testGetSignatureDetailsFromPlatformWithValidSignature() {
        PlatformSignatureDetails platformSignatureDetails = PlatformSignatureDetails.builder()
                .status(applicationConfiguration.getSignature().getStatus().getCompleted()).build();
        when(signatureClient.getSignatureDetails("signatureId")).thenReturn(platformSignatureDetails);

        assertNotNull(signatureService.getSignatureDetailsFromPlatform("signatureId"));
    }

    @Test
    public void testGetSignatureDetailsFromPlatformWithInvalidSignature() {
        PlatformSignatureDetails platformSignatureDetails = PlatformSignatureDetails.builder()
                .status(SignatureStatus.builder().pending("PENDING").build().getPending()).build();
        when(signatureClient.getSignatureDetails("signatureId")).thenReturn(platformSignatureDetails);

        assertNotNull(signatureService.getSignatureDetailsFromPlatform("signatureId"));
        assertEquals(LiteralConstants.SIGNATURE_NOT_ADDED_DUE_TO, signatureService.getSignatureDetailsFromPlatform("signatureId").getErrorCode());
    }

    @Test
    public void testDeleteByApplicationNumber(){
        when(applicationService.getApplicationsByNumberAndLock("applicationNumber", USERNAME))
                .thenReturn(Collections.singletonList(applicationTrademark));
        signatureService.deleteByApplicationNumber(USERNAME,"applicationNumber");
        verify(shoppingCartService,times(1)).removeApplication(applicationTrademark.getId());
    }

}