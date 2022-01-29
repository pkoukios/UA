/*
 * $Id:: ExcelServiceImplTest.java 2021/03/01 09:07 dvelegra
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
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.FilteringData;
import eu.euipo.etmdn.userarea.common.domain.PaginationData;
import eu.euipo.etmdn.userarea.common.domain.SortingData;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExcelServiceImplTest {

    @InjectMocks
    private ExcelServiceImpl excelService;

    @Mock
    private ApplicationService applicationService;

    private ApplicationSearchResult pageableApplicationTrademark;
    private ApplicationSearchResult pageableApplicationDesign;
    private ApplicationSearchResult pageableApplicationEservice;
    private ApplicationRequest applicationRequestTrademark;
    private ApplicationRequest applicationRequestDesign;
    private ApplicationRequest applicationRequestEservice;

    private final Set<String> roles = new HashSet<>();
    private static final String USERNAME = "abc@xyz.com";

    @Before
    public void setUp() {
        excelService = new ExcelServiceImpl(applicationService);
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
    }

    @Test
    public void testGenerateTrademarkToExcel() {
        applicationRequestTrademark.setIsDraft(false);
        when(applicationService.getApplications(applicationRequestTrademark, roles)).thenReturn(pageableApplicationTrademark);
        FileInfo fileInfo = excelService.generateExcel(applicationRequestTrademark, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDesignToExcel() {
        applicationRequestDesign.setIsDraft(false);
        when(applicationService.getApplications(applicationRequestDesign, roles)).thenReturn(pageableApplicationDesign);
        FileInfo fileInfo = excelService.generateExcel(applicationRequestDesign, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateEserviceToExcel() {
        applicationRequestEservice.setIsDraft(false);
        when(applicationService.getApplications(applicationRequestEservice, roles)).thenReturn(pageableApplicationEservice);
        FileInfo fileInfo = excelService.generateExcel(applicationRequestEservice, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDraftTrademarkToExcel() {
        applicationRequestTrademark.setIsDraft(true);
        when(applicationService.getApplications(applicationRequestTrademark, roles)).thenReturn(pageableApplicationTrademark);
        FileInfo fileInfo = excelService.generateExcel(applicationRequestTrademark, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDraftDesignToExcel() {
        applicationRequestDesign.setIsDraft(true);
        when(applicationService.getApplications(applicationRequestDesign, roles)).thenReturn(pageableApplicationDesign);
        FileInfo fileInfo = excelService.generateExcel(applicationRequestDesign, roles);
        assertNotNull(fileInfo);
    }

    @Test
    public void testGenerateDraftEserviceToExcel() {
        applicationRequestEservice.setIsDraft(true);
        when(applicationService.getApplications(applicationRequestEservice, roles)).thenReturn(pageableApplicationEservice);
        FileInfo fileInfo = excelService.generateExcel(applicationRequestEservice, roles);
        assertNotNull(fileInfo);
    }

}