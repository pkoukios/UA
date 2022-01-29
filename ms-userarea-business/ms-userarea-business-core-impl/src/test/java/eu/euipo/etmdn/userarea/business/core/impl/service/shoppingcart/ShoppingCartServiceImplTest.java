/*
 * $Id:: ShoppingCartServiceImplTest.java 2021/09/09 05:30 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service.shoppingcart;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.SignatureService;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationStatus;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.correspondence.search.SearchMessageType;
import eu.euipo.etmdn.userarea.common.domain.exception.EntityNotFoundException;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.common.persistence.entity.MainAccount;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchCriteriaShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchShoppingCartSort;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;
import eu.euipo.etmdn.userarea.domain.shoppingcart.exception.ShoppingCartSecurityException;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.shoppingcart.ShoppingCartEntity;
import eu.euipo.etmdn.userarea.persistence.mapper.shoppingcart.ShoppingCartMapper;
import eu.euipo.etmdn.userarea.persistence.repository.shoppingcart.ShoppingCartApplicationRepository;
import eu.euipo.etmdn.userarea.persistence.repository.shoppingcart.ShoppingCartRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingCartServiceImplTest {

    @InjectMocks
    ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ShoppingCartApplicationRepository shoppingCartApplicationRepository;

    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private AccountService accountService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private SignatureService signatureService;

    private SearchCriteriaShoppingCart searchCriteriaShoppingCart;
    private ShoppingCartEntity shoppingCartEntity;
    private ShoppingCart shoppingCart;
    private ShoppingCartApplicationEntity shoppingCartApplicationEntity;
    private MainAccount mainAccount;
    private DomainAccount domainAccount;
    private static final String USERNAME = "abc@xyz.com";
    private Application applicationTrademark;
    private final Set<String> roles = new HashSet<>();

    @Before
    public void setUp() {
        searchCriteriaShoppingCart = new SearchCriteriaShoppingCart();

        shoppingCartEntity = new ShoppingCartEntity();
        shoppingCartEntity.setId(1L);
        shoppingCartEntity.setCreatedDate(LocalDateTime.now());
        shoppingCartEntity.setUser(USERNAME);
        shoppingCartApplicationEntity = new ShoppingCartApplicationEntity();
        shoppingCartApplicationEntity.setId(1L);
        shoppingCartApplicationEntity.setApplicant("Applicant");
        shoppingCartApplicationEntity.setShoppingCart(shoppingCartEntity);
        shoppingCartApplicationEntity.setApplicationId(1L);
        shoppingCartApplicationEntity.setFees(new BigDecimal(400));
        shoppingCartApplicationEntity.setFoModule("trademark");
        shoppingCartApplicationEntity.setIsDesign(false);
        shoppingCartApplicationEntity.setIsTrademark(true);
        shoppingCartApplicationEntity.setLastModifiedBy("user");
        shoppingCartApplicationEntity.setLastModifiedDate(LocalDateTime.now());
        List<ShoppingCartApplicationEntity> list = new ArrayList<>();
        list.add(shoppingCartApplicationEntity);
        shoppingCartEntity.setApplications(list);
        shoppingCart = ShoppingCartMapper.MAPPER.mapToDomain(shoppingCartEntity);
        mainAccount = new MainAccount();
        mainAccount.setUsername(USERNAME);
        applicationTrademark = Application.builder().number("12345678").foModule("trademark").type("Word")
                .kind("Individual").status("Pending Signature")
                .build();
        ApplicationStatus status = ApplicationStatus.builder().payment("Pending payment").build();
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStatus(status);
        shoppingCartService = new ShoppingCartServiceImpl(shoppingCartRepository,shoppingCartApplicationRepository,applicationConfiguration,accountService,signatureService);
        domainAccount = new DomainAccount();
        domainAccount.setUsername(USERNAME);
        roles.addAll(Arrays.asList("ROLE_TRADEMARKS", "ROLE_SIGNATURES"));
    }

    @Test
    public void testCreateNewShoppingCart(){
        when(shoppingCartRepository.save(any())).thenReturn(shoppingCartEntity);
        ShoppingCart shoppingCart = shoppingCartService.create(USERNAME);
        assertNotNull(shoppingCart);
        assertEquals(this.shoppingCart,shoppingCart);
    }

    @Test
    public void testGetShoppingCartByUser(){
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        ShoppingCart shoppingCart = shoppingCartService.getByUser(USERNAME);
        assertNotNull(shoppingCart);
        assertEquals(this.shoppingCart,shoppingCart);
    }

    @Test
    public void testGetShoppingCartById(){
        when(shoppingCartRepository.getById(1L)).thenReturn(Optional.of(shoppingCartEntity));
        ShoppingCart shoppingCart = shoppingCartService.getById(1L);
        assertNotNull(shoppingCart);
        assertEquals(this.shoppingCart,shoppingCart);
    }

    @Test
    public void testApplicationIsNotAddedToShoppingCartDueToWrongStatus(){
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark,domainAccount.getUsername());
        verify(shoppingCartApplicationRepository,times(0)).save(any());
    }

    @Test
    public void testCheckAndAddApplicationToShoppingCartWithNullShoppingCart(){
        applicationTrademark.setStatus("Pending payment");
        applicationTrademark.setMainAccount(mainAccount);
        ApplicationStatus status = ApplicationStatus.builder().payment("Pending payment").build();
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStatus(status);
        shoppingCartService = new ShoppingCartServiceImpl(shoppingCartRepository,shoppingCartApplicationRepository,applicationConfiguration,accountService, signatureService);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.empty());
        when(shoppingCartApplicationRepository.save(any())).thenReturn(shoppingCartApplicationEntity);
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(any())).thenReturn(Optional.of(shoppingCartApplicationEntity));
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark, domainAccount.getUsername());
        verify(shoppingCartRepository,times(1)).save(any());
    }

    @Test
    public void testApplicationIsAddedToShoppingCartDueToCorrectStatus(){
        applicationTrademark.setStatus("Pending payment");
        applicationTrademark.setMainAccount(mainAccount);
        ApplicationStatus status = ApplicationStatus.builder().payment("Pending payment").build();
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStatus(status);
        shoppingCartService = new ShoppingCartServiceImpl(shoppingCartRepository,shoppingCartApplicationRepository,applicationConfiguration,accountService, signatureService);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        when(shoppingCartApplicationRepository.save(any())).thenReturn(shoppingCartApplicationEntity);
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(any())).thenReturn(Optional.of(shoppingCartApplicationEntity));
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark, domainAccount.getUsername());
        verify(shoppingCartApplicationRepository,times(1)).save(any());

    }

    private void initValuesForApplicationIsAddedToShoppingCart(){
        applicationTrademark.setStatus("Pending payment");
        applicationTrademark.setMainAccount(mainAccount);
        ApplicationStatus status = ApplicationStatus.builder().payment("Pending payment").build();
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStatus(status);
        shoppingCartService = new ShoppingCartServiceImpl(shoppingCartRepository,shoppingCartApplicationRepository,applicationConfiguration,accountService, signatureService);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        when(shoppingCartApplicationRepository.save(any())).thenReturn(shoppingCartApplicationEntity);
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(any())).thenReturn(Optional.empty());
    }

    @Test
    public void testApplicationIsAddedToShoppingCartForTrademarkType(){
        initValuesForApplicationIsAddedToShoppingCart();
        applicationTrademark.setFoModule(ApplicationType.TRADEMARK.value);
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark, domainAccount.getUsername());
        verify(shoppingCartApplicationRepository,times(1)).save(any());
    }

    @Test
    public void testApplicationIsAddedToShoppingCartForDesignType(){
        initValuesForApplicationIsAddedToShoppingCart();
        applicationTrademark.setFoModule(ApplicationType.DESIGN.value);
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark, domainAccount.getUsername());
        verify(shoppingCartApplicationRepository,times(1)).save(any());
    }

    @Test
    public void testApplicationIsAddedToShoppingCartForEserviceTypeDesignMessageType(){
        initValuesForApplicationIsAddedToShoppingCart();
        applicationTrademark.setFoModule(ApplicationType.ESERVICE.value);
        applicationTrademark.setIpRightType(SearchMessageType.DESIGNS.getValue());
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark, domainAccount.getUsername());
    }

    @Test
    public void testApplicationIsAddedToShoppingCartForEserviceTypeTrademarksMessageType(){
        initValuesForApplicationIsAddedToShoppingCart();
        applicationTrademark.setFoModule(ApplicationType.ESERVICE.value);
        applicationTrademark.setIpRightType(SearchMessageType.TRADEMARKS.getValue());
        shoppingCartService.checkAndAddApplicationToShoppingCart(mainAccount,applicationTrademark, domainAccount.getUsername());
    }

    @Test
    public void testGetShoppingCartApplications(){
        SearchCriteriaShoppingCart searchCriteriaShoppingCart = new SearchCriteriaShoppingCart();
        searchCriteriaShoppingCart.setSortType("ASC");
        searchCriteriaShoppingCart.setSort(SearchShoppingCartSort.TYPE);
        List<ShoppingCartApplicationEntity> list = new ArrayList<>();
        list.add(shoppingCartApplicationEntity);
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        when(shoppingCartApplicationRepository.findAll(any(Specification.class))).thenReturn(list);
        ShoppingCartSearch search = shoppingCartService.getApplications(USERNAME,searchCriteriaShoppingCart,roles);
        assertNotNull(search);
        assertEquals(1, search.getContent().size());

        searchCriteriaShoppingCart.setSortType("DESC");
        ShoppingCartSearch searchDesc = shoppingCartService.getApplications(USERNAME,searchCriteriaShoppingCart,roles);
        assertNotNull(searchDesc);
        assertEquals(1, searchDesc.getContent().size());
    }

    @Test
    public void testGetShoppingCartApplicationsWithNullShoppingCart(){
        SearchCriteriaShoppingCart searchCriteriaShoppingCart = new SearchCriteriaShoppingCart();
        searchCriteriaShoppingCart.setSortType("ASC");
        searchCriteriaShoppingCart.setSort(SearchShoppingCartSort.TYPE);
        List<ShoppingCartApplicationEntity> list = new ArrayList<>();
        list.add(shoppingCartApplicationEntity);
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.empty());
        ShoppingCartSearch search = shoppingCartService.getApplications(USERNAME,searchCriteriaShoppingCart,roles);
        assertNotNull(search);
        assertEquals(0, search.getContent().size());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testModifyApplicationFromShoppingCartThrowsErrorWhenShoppingCartDoesnExist(){
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.empty());
        shoppingCartService.modifyApplication(USERNAME,"",false, false);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testModifyApplicationFromShoppingCartThrowsErrorWhenApplicationDoesnExist(){
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(123L)).thenReturn(Optional.empty());
        shoppingCartService.modifyApplication(USERNAME,"123",false, false);
    }

    @Test(expected = ShoppingCartSecurityException.class)
    public void testModifyApplicationFromShoppingCartThrowsShoppingCartSecurityException(){
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        ShoppingCartEntity shoppingCart = new ShoppingCartEntity();
        shoppingCart.setId(234L);
        shoppingCartApplicationEntity.setShoppingCart(shoppingCart);
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(123L)).thenReturn(Optional.of(shoppingCartApplicationEntity));
        shoppingCartService.modifyApplication(USERNAME,"123",false, false);
    }

    @Test
    public void testModifyApplicationFromShoppingCartPasses(){
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(123L)).thenReturn(Optional.of(shoppingCartApplicationEntity));
        ApplicationStatus status = ApplicationStatus.builder().draft("Initialized").build();
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStatus(status);
        shoppingCartService = new ShoppingCartServiceImpl(shoppingCartRepository,shoppingCartApplicationRepository,applicationConfiguration,accountService, signatureService);
        shoppingCartService.modifyApplication(USERNAME,"123",false, false);
        verify(signatureService,times(1)).modifyApplication(any(),any());
    }

    @Test
    public void testDeleteApplicationFromShoppingCartPasses(){
        when(accountService.getMainAccount(USERNAME)).thenReturn(domainAccount);
        when(shoppingCartRepository.getByUser(USERNAME)).thenReturn(Optional.of(shoppingCartEntity));
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(123L)).thenReturn(Optional.of(shoppingCartApplicationEntity));
        ApplicationStatus status = ApplicationStatus.builder().draft("Initialized").build();
        applicationConfiguration = new ApplicationConfiguration();
        applicationConfiguration.setStatus(status);
        shoppingCartService = new ShoppingCartServiceImpl(shoppingCartRepository,shoppingCartApplicationRepository,applicationConfiguration,accountService, signatureService);
        shoppingCartService.modifyApplication(USERNAME,"123",true, false);
        verify(signatureService,times(1)).deleteApplication(any(),any());
    }

    @Test
    public void testRemoveApplication() {
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(1L))
                .thenReturn(Optional.ofNullable(shoppingCartApplicationEntity));
        shoppingCartService.removeApplication(1L);
        verify(shoppingCartApplicationRepository,times(1)).delete(shoppingCartApplicationEntity);
    }

    @Test
    public void testRemoveApplicationWhenApplicationDoesNotExist() {
        when(shoppingCartApplicationRepository.getShoppingCartApplicationEntityByApplicationId(1L)).thenReturn(Optional.empty());
        shoppingCartService.removeApplication(1L);
        verify(shoppingCartApplicationRepository,times(0)).delete(shoppingCartApplicationEntity);
    }

    @Test
    public void testGetShoppingCartApplicationsByIds() {
        when(shoppingCartApplicationRepository.findByApplicationIdIn(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(shoppingCartApplicationEntity));
        List<ShoppingCartApplication> shoppingCartApplicationsList = shoppingCartService.getShoppingCartApplicationsByIds(Collections.singletonList(1L));
        assertNotNull(shoppingCartApplicationsList);
        assertEquals(1, shoppingCartApplicationsList.size());
        verify(shoppingCartApplicationRepository,times(1)).findByApplicationIdIn(anyList());
    }

    @Test
    public void testGetShoppingCartApplicationsByNumbers() {
        when(shoppingCartApplicationRepository.findByNumberIn(Collections.singletonList("123456")))
                .thenReturn(Collections.singletonList(shoppingCartApplicationEntity));
        List<ShoppingCartApplication> shoppingCartApplicationsList = shoppingCartService.getShoppingCartApplicationsByNumbers(Collections.singletonList("123456"));
        assertNotNull(shoppingCartApplicationsList);
        assertEquals(1, shoppingCartApplicationsList.size());
        verify(shoppingCartApplicationRepository,times(1)).findByNumberIn(anyList());
    }


}
