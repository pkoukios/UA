/*
 * $Id:: ChildServiceImplTest.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.business.service.AuthorityService;
import eu.euipo.etmdn.userarea.common.business.service.RegistrationService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.AccountType;
import eu.euipo.etmdn.userarea.common.domain.ChildInformation;
import eu.euipo.etmdn.userarea.common.domain.ChildUpdateDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DomainAuthority;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.persistence.entity.ChildAccount;
import eu.euipo.etmdn.userarea.common.persistence.repository.ChildAccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChildServiceImplTest {

    @InjectMocks
    private ChildServiceImpl childService;

    @Mock
    private ChildAccountRepository childAccountRepository;
    @Mock
    private UserService userService;
    @Mock
    private RegistrationService registrationService;
    @Mock
    private AuthorityService authorityService;
    @Mock
    private AccountService accountService;

    private DomainUser user;
    private DomainUser childUser;
    private ChildAccount child;
    private Set<DomainAuthority> authorities;
    private final Integer page= 0;
    private final Integer size = 1;
    private final String sortColumn = "firstName";
    private final String sortType = "DESC";
    private Page<ChildAccount> pageableChild;
    private ChildUpdateDetailsRequest childUpdateDetailsRequest;
    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";

    @Before
    public void setUp() {
        childService = new ChildServiceImpl(childAccountRepository, userService, registrationService, authorityService, accountService);
        child = ChildAccount.builder().mainAccountName(username).username(username).firstName("John").surName("Cool").email(email)
                .roles("ROLE_TRADEMARKS,ROLE_APPLICATION_SUBMIT").build();
        DomainAuthority authorityTrademark = DomainAuthority.builder().role("ROLE_APPLICATION_SUBMIT").dependency("ROLE_TRADEMARKS").build();
        authorities = new HashSet<>(Collections.singletonList(authorityTrademark));
        DomainAccount mainAccount = DomainAccount.builder().username(username).firstName("John").surName("Cool").email(email).type(AccountType.PARENT.name()).build();
        DomainAccount childDomainAccount = DomainAccount.builder().username(username).firstName("John").surName("Cool").email(email).type(AccountType.CHILD.name()).build();
        user = DomainUser.builder()
                .username(username)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .authorities(authorities)
                .domainAccount(mainAccount)
                .build();
        childUser = DomainUser.builder()
                .username(username)
                .enabled(true)
                .accountNonLocked(true)
                .authorities(authorities)
                .domainAccount(childDomainAccount)
                .build();
        pageableChild = new PageImpl<>(Collections.singletonList(child));
        childUpdateDetailsRequest = ChildUpdateDetailsRequest.builder().userName(username).firstName("John").lastName("Cool")
                .roles(Collections.unmodifiableList(Arrays.asList("ROLE_TRADEMARKS", "ROLE_APPLICATION_SUBMIT")))
                .build();
    }

    @Test
    public void testGetParentChildAccountsSuccess() {
        when(authorityService.getAllAuthorities()).thenReturn(new ArrayList<>(authorities));
        when(userService.getUserByUsername(username)).thenReturn(user);
        when(childAccountRepository.findByMainAccountName(username,
                PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortColumn)))).thenReturn(pageableChild);
        Page<ChildInformation> pageableChild = childService.getChildAccounts(username, page, size, sortColumn, sortType);
        assertNotNull(pageableChild);
    }

    @Test
    public void testGetChildAccountsSuccess() {
        when(userService.getUserByUsername(username)).thenReturn(childUser);
        when(childAccountRepository.findByUsername(username)).thenReturn(child);
        when(childAccountRepository.findByMainAccountName(username,
                PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortColumn)))).thenReturn(pageableChild);
        Page<ChildInformation> pageableChild = childService.getChildAccounts(username, page, size, sortColumn, sortType);
        assertNotNull(pageableChild);
    }

    @Test
    public void testGetChildAccountsNotValidAccount() {
        Page<ChildInformation> pageableChild = childService.getChildAccounts(null, page, size, sortColumn, sortType);
        assertNull(pageableChild);
    }

    @Test
    public void testUpdateChildAccountSuccess() {
        when(userService.checkRolesWithDependencies(any(), any())).thenReturn(true);
        when(childAccountRepository.findByUsername(username)).thenReturn(child);
        when(userService.getUserByUsername(child.getUsername())).thenReturn(user);
        AccountResponse response = childService.update(childUpdateDetailsRequest, username);
        assertNotNull(response);
        verify(childAccountRepository, times(1)).save(any(ChildAccount.class));
    }

    @Test
    public void testUpdateChildAccountFailDueToRoles() {
        when(userService.checkRolesWithDependencies(any(), any())).thenReturn(false);
        AccountResponse response = childService.update(childUpdateDetailsRequest, username);
        assertNotNull(response);
        verify(childAccountRepository, times(0)).save(any(ChildAccount.class));
    }

    @Test
    public void testUpdateChildAccountFailNotExist() {
        when(userService.checkRolesWithDependencies(any(), any())).thenReturn(true);
        AccountResponse response = childService.update(childUpdateDetailsRequest, username);
        assertNull(response);
        verify(childAccountRepository, times(0)).save(any(ChildAccount.class));
    }

    /*
    @Test
    public void testActivateChildAccountSuccess() {
        when(childAccountRepository.findByUsername(username)).thenReturn(child);
        when(userService.getUserByUsername(username)).thenReturn(childUser);
        when(userService.activate(childUser)).thenReturn(childUser);
        when(registrationService.sendEmailRegistration(childUser)).thenReturn(emailUser);
        AccountResponse response = childService.activate(username);
        assertNotNull(response);
        verify(childAccountRepository, times(1)).save(any(ChildAccount.class));
    }

    @Test
    public void testActivateChildAccountFail() {
        when(childAccountRepository.findByUsername(username)).thenReturn(null);
        AccountResponse response = childService.activate(username);
        assertNull(response);
        verify(childAccountRepository, times(0)).save(any(ChildAccount.class));
    }

    @Test
    public void testDeActivateChildAccountSuccess() {
        when(childAccountRepository.findByUsername(username)).thenReturn(child);
        when(userService.getUserByUsername(username)).thenReturn(childUser);
        AccountResponse response = childService.deactivate(username);
        assertNotNull(response);
        verify(childAccountRepository, times(1)).save(any(ChildAccount.class));
    }

    @Test
    public void testDeActivateChildAccountFail() {
        when(childAccountRepository.findByUsername(username)).thenReturn(null);
        AccountResponse response = childService.deactivate(username);
        assertNull(response);
        verify(childAccountRepository, times(0)).save(any(ChildAccount.class));
    }

     */

}