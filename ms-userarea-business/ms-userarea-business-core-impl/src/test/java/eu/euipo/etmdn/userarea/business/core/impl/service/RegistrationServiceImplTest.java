/*
 * $Id:: RegistrationServiceImplTest.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.event.AuditEventPublisher;
import eu.euipo.etmdn.userarea.common.business.service.AccountEmailNotificationService;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.business.service.PasswordValidationService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.business.service.UserTokenService;
import eu.euipo.etmdn.userarea.common.business.service.UsernameValidationService;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.ChildDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.ChildUpdateDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.DomainUserToken;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.email.EmailUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationServiceImplTest {

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Mock
    private UserService userService;
    @Mock
    private AccountService accountService;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private UsernameValidationService usernameValidationService;
    @Mock
    private PasswordValidationService passwordValidationService;
    @Mock
    private AccountEmailNotificationService accountEmailNotificationService;
    @Mock
    private AuditEventPublisher auditEventPublisher;

    private DomainUser user;
    private DomainAccount domainAccount;
    private EmailUser emailUser;
    private DomainUserToken domainUserToken;
    private ChildUpdateDetailsRequest childUpdateDetailsRequest;
    private ChildDetailsRequest childDetailsRequest;
    private AccountRegistrationRequest accountRegistrationRequest;
    private ConfirmRegistrationRequest confirmRegistrationRequest;

    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";
    private static final String token = "aaa111";
    private static final String password = "12345";

    @Before
    public void setUp() {
        registrationService = new RegistrationServiceImpl(userService, accountService, userTokenService,
                usernameValidationService, passwordValidationService, accountEmailNotificationService, auditEventPublisher);
        domainAccount = DomainAccount.builder().id(1L).email(email).firstName("John").surName("Cool").build();
        emailUser = EmailUser.builder().emailName("ABC DEF").token("12345").email(email).build();
        user = DomainUser.builder()
                .username(username)
                .password(password)
                .enabled(true)
                .accountNonLocked(true)
                .domainAccount(domainAccount)
                .build();
        domainUserToken = DomainUserToken.builder()
                .token(token)
                .username(username)
                .expirationDateTime(LocalDateTime.now().plusSeconds(7200L))
                .build();
        accountRegistrationRequest = AccountRegistrationRequest.builder()
                .userName(username)
                .email(email)
                .language(Locale.getDefault().getLanguage())
                .build();
        confirmRegistrationRequest = ConfirmRegistrationRequest.builder()
                .token(token)
                .build();
        childDetailsRequest = ChildDetailsRequest.builder().email(username).firstName("John").lastName("Cool")
                .roles(Collections.unmodifiableList(Arrays.asList("ROLE_TRADEMARKS", "ROLE_APPLICATION_SUBMIT")))
                .build();
        childUpdateDetailsRequest = ChildUpdateDetailsRequest.builder().userName(username).firstName("John").lastName("Cool")
                .roles(Collections.unmodifiableList(Arrays.asList("ROLE_TRADEMARKS", "ROLE_APPLICATION_SUBMIT")))
                .build();
    }

    @Test
    public void testValidateRegistration() {
        doNothing().when(usernameValidationService).validate(username, email);
        registrationService.validate(username, email, password);
    }

    @Test
    public void testParentUserRegistrationCreationSuccess() {
        accountRegistrationRequest.setPassword(password);
        when(userService.registerUser(username, password, domainAccount)).thenReturn(user);
        when(accountEmailNotificationService.sendRegistrationEmail(user.getDomainAccount())).thenReturn(emailUser);
        EmailUser registeredUser = registrationService.register(accountRegistrationRequest, domainAccount);
        Assert.assertNotNull(registeredUser);
    }

    @Test
    public void testChildRegistrationCreationSuccess() {
        doNothing().when(usernameValidationService).validate(childUpdateDetailsRequest.getUserName(), childDetailsRequest.getEmail());
        when(userService.registerChild(childDetailsRequest, username)).thenReturn(user);
        AccountRegistrationResponse response = registrationService.registerChild(childDetailsRequest, username);
        Assert.assertNotNull(response);
    }

    @Test
    public void testChildRegistrationCreationFail() {
        doNothing().when(usernameValidationService).validate(childUpdateDetailsRequest.getUserName(), childDetailsRequest.getEmail());
        when(userService.registerChild(childDetailsRequest, username)).thenReturn(null);
        AccountRegistrationResponse response = registrationService.registerChild(childDetailsRequest, username);
        Assert.assertNotNull(response);
        Assert.assertEquals(LiteralConstants.ENABLE_IP_RIGHT_ACCESS, response.getMessage());
    }

    @Test
    public void testConfirmationRegistrationCreationSuccess() {
        when(userTokenService.validateTokenExpiration(token)).thenReturn(domainUserToken);
        when(accountService.verifyAccountRegistration(domainUserToken.getUsername())).thenReturn(domainAccount);
        doNothing().when(userTokenService).inValidateToken(domainUserToken);
        ConfirmRegistrationResponse response = registrationService.confirm(confirmRegistrationRequest);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isValid());
    }

    @Test
    public void testChildConfirmationRegistrationCreationSuccess() {
        confirmRegistrationRequest.setPassword(password);
        when(userTokenService.validateTokenExpiration(token)).thenReturn(domainUserToken);
        when(accountService.verifyAccountRegistration(domainUserToken.getUsername())).thenReturn(domainAccount);
        when(userService.getUserByUsername(username)).thenReturn(user);
        doNothing().when(userTokenService).inValidateToken(domainUserToken);
        ConfirmRegistrationResponse response = registrationService.confirm(confirmRegistrationRequest);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isValid());
    }

    @Test
    public void testConfirmationRegistrationCreationFail() {
        when(userTokenService.validateTokenExpiration(token)).thenReturn(null);
        ConfirmRegistrationResponse response = registrationService.confirm(confirmRegistrationRequest);
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isValid());
    }

    @Test
    public void testRegistrationResendTokenSuccessByEmail() {
        when(userTokenService.getUserTokenByToken(token)).thenReturn(domainUserToken);
        when(userService.getUserByUsername(username)).thenReturn(user);
        when(accountEmailNotificationService.sendRegistrationEmail(user.getDomainAccount())).thenReturn(emailUser);
        ConfirmRegistrationResponse response = registrationService.resendToken(confirmRegistrationRequest);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isValid());
    }

    @Test
    public void testRegistrationResendTokenSuccessByUsername() {
        ConfirmRegistrationRequest request = ConfirmRegistrationRequest.builder().userName(username).build();
        when(userService.getUserByUsername(username)).thenReturn(user);
        when(accountEmailNotificationService.sendRegistrationEmail(user.getDomainAccount())).thenReturn(emailUser);
        ConfirmRegistrationResponse response = registrationService.resendToken(request);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isValid());
    }

    @Test
    public void testRegistrationResendTokenFail() {
        when(userTokenService.getUserTokenByToken(token)).thenReturn(null);
        ConfirmRegistrationResponse response = registrationService.resendToken(confirmRegistrationRequest);
        Assert.assertNull(response);
    }

}