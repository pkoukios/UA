/*
 * $Id:: UserProfileServiceImplTest.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.domain.AccountRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.AddressType;
import eu.euipo.etmdn.userarea.common.domain.ApplicantType;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DomainAddress;
import eu.euipo.etmdn.userarea.common.domain.DomainAuthority;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.MainAccountType;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.persistence.entity.ChildAccount;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileServiceImplTest {

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Mock
    private UserService userService;

    private DomainUser user;
    private ChildAccount child;
    private AccountRequest accountRequest;
    private String hashedPassword;
    private final List<DomainAddress> addressList = new ArrayList<>();
    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";
    private static final String password = "12345";
    private static final String language = "el";

    @Before
    public void setUp() {
        userProfileService = new UserProfileServiceImpl(userService, new BCryptPasswordEncoder());
        hashedPassword = new BCryptPasswordEncoder().encode(password);
        accountRequest = AccountRequest.builder().userName(username).language(language).build();
        DomainAddress legalAddress = DomainAddress.builder().streetName("Via Victoria").streetNumber("10")
                .addressType(AddressType.LEGAL.getValue()).city("Alicante").build();
        addressList.add(legalAddress);
        DomainAccount person = DomainAccount.builder().firstName("John").surName("Cool").email(email).language(language)
                .mainAccountType(MainAccountType.APPLICANT.getValue())
                .legalType(ApplicantType.INDIVIDUAL.getValue())
                .nationality("EN")
                .addresses(new ArrayList<>(addressList))
                .corrAddress(false)
                .build();
        user = DomainUser.builder()
                .id(1L)
                .username(username)
                .password(password)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .domainAccount(person)
                .authorities(Collections.singleton(DomainAuthority.builder().id(1L).role("ROLE_TRADEMARKS").build()))
                .build();
        child = ChildAccount.builder().mainAccountName(username).username(username).firstName("John").surName("Cool").email(email).build();
    }

    @Test
    public void testChangePasswordSuccess() {
        accountRequest.setOldPassword(password);
        accountRequest.setNewPassword("azws03E");
        user.setPassword(hashedPassword);
        when(userService.getUserByUsername(username)).thenReturn(user);
        AccountResponse response = userProfileService.changePassword(accountRequest);
        Assert.assertNotNull(response);
        assertEquals(user.getUsername(), response.getUserName());
        assertTrue(response.isValid());
        assertEquals(LiteralConstants.PASSWORD_CHANGE_SUCCESS, response.getMessage());
    }

    @Test
    public void testChangePasswordNotMatch() {
        accountRequest.setOldPassword(password);
        accountRequest.setNewPassword("azws03E");
        when(userService.getUserByUsername(username)).thenReturn(user);
        AccountResponse response = userProfileService.changePassword(accountRequest);
        Assert.assertNotNull(response);
        assertEquals(user.getUsername(), response.getUserName());
        assertFalse(response.isValid());
        assertEquals(LiteralConstants.PASSWORD_NOT_MATCH, response.getMessage());
    }

    @Test
    public void testChangePasswordNotExistingAccount() {
        when(userService.getUserByUsername(username)).thenReturn(null);
        AccountResponse response = userProfileService.changePassword(accountRequest);
        Assert.assertNull(response);
    }
    /*

    @Test
    public void testValidationWhenUserDoesNotExist() {
        DeactivateAccountResponse result = userProfileService.validateDeactivation(username);
        Assert.assertNull(result);
    }

    @Test
    public void testDeactivationValidationWhenUserIsNotEnabled() {
        user.setEnabled(Boolean.FALSE);
        DeactivateAccountResponse result = userProfileService.validateDeactivation(username);
        Assert.assertNull(result);
    }

    @Test
    public void testDeactivationValidationWhenUserIsEnabledAndDoesNotHaveActiveChildAccounts() {
        when(userService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(childAccountRepository.findByMainAccountNameAndStatusIsIn(user.getUsername(), Arrays.asList("Active", "Locked"))).thenReturn(new ArrayList<>());
        DeactivateAccountResponse result = userProfileService.validateDeactivation(username);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isValid());
        assertEquals(LiteralConstants.DEACTIVATE_ACCOUNT_QUESTION, result.getMessage());
        assertEquals(user.getUsername(), result.getUserName());
    }

    @Test
    public void testDeactivationValidationWhenUserIsEnabledButHasActiveChildAccounts() {
        when(userService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(childAccountRepository.findByMainAccountNameAndStatusIsIn(user.getUsername(), Arrays.asList("Active", "Locked")))
                .thenReturn(Arrays.asList(child, ChildAccount.builder().build()));
        DeactivateAccountResponse result = userProfileService.validateDeactivation(username);
        Assert.assertNotNull(result);
        Assert.assertFalse("The request should be invalid", result.isValid());
        assertEquals(LiteralConstants.DEACTIVATE_ACCOUNT_INFO, result.getMessage());
        assertEquals(user.getUsername(), result.getUserName());
    }

    @Test
    public void testDeactivationConfirmationWhenUserIsEnabledAndDoesNotHaveActiveChildAccounts() {
        when(userService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(childAccountRepository.findByMainAccountNameAndStatusIsIn(user.getUsername(), Arrays.asList("Active", "Locked"))).thenReturn(new ArrayList<>());
        DeactivateAccountResponse result = userProfileService.confirmDeactivation(username);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isValid());
        assertEquals(LiteralConstants.DEACTIVATE_ACCOUNT_SUCCESS, result.getMessage());
        assertEquals(user.getUsername(), result.getUserName());
    }

    @Test
    public void testDeactivationConfirmationWhenUserIsEnabledButHasActiveChildAccounts() {
        when(userService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(childAccountRepository.findByMainAccountNameAndStatusIsIn(user.getUsername(), Arrays.asList("Active", "Locked")))
                .thenReturn(Arrays.asList(child, ChildAccount.builder().build()));
        DeactivateAccountResponse result = userProfileService.confirmDeactivation(username);
        Assert.assertNotNull(result);
        Assert.assertFalse("The request should be invalid", result.isValid());
        assertEquals(LiteralConstants.DEACTIVATE_ACCOUNT_INFO, result.getMessage());
        assertEquals(user.getUsername(), result.getUserName());
    }

    @Test
    public void testDeactivateChildAccount() {
        user.getDomainAccount().setType(AccountType.CHILD.name());
        when(userService.getUserByUsername(username)).thenReturn(user);
        when(childAccountRepository.findByMainAccountNameAndStatusIsIn(user.getUsername(), Arrays.asList("Active", "Locked"))).thenReturn(new ArrayList<>());
        DeactivateAccountResponse result = userProfileService.confirmDeactivation(username);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isValid());
        assertEquals(LiteralConstants.DEACTIVATE_ACCOUNT_SUCCESS, result.getMessage());
        assertEquals(user.getUsername(), result.getUserName());
    }

    @Test
    public void testDeactivationConfirmationWhenUserIsNotEnabled() {
        user.setEnabled(Boolean.FALSE);
        DeactivateAccountResponse result = userProfileService.confirmDeactivation(username);
        Assert.assertNull(result);
    }

     */
}