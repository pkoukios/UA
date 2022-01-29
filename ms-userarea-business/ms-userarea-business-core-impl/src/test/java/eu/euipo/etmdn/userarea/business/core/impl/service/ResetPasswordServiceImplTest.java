/*
 * $Id:: ResetPasswordServiceImplTest.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.EmailService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.business.service.UserTokenService;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.DomainUserToken;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenRequest;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenResponse;
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

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResetPasswordServiceImplTest {

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;

    @Mock
    private UserService userService;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private EmailService emailService;

    private DomainUser user;
    private EmailUser emailUser;
    private DomainUserToken userToken;
    private ConfirmPasswordResetRequest confirmPasswordResetRequest;
    private ValidateTokenRequest validateTokenRequest;

    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";
    private static final String token = "aaa111";
    private static final String password = "12345";

    @Before
    public void setUp() {
        resetPasswordService = new ResetPasswordServiceImpl(userTokenService, userService, emailService);
        emailUser = EmailUser.builder().emailName("ABC DEF").token("12345").email(email).build();
        DomainAccount domainAccount = DomainAccount.builder().id(1L).email(email).firstName("John").surName("Cool").build();
        user = DomainUser.builder()
                .username(username)
                .password(password)
                .enabled(true)
                .accountNonLocked(true)
                .domainAccount(domainAccount)
                .build();
        userToken = DomainUserToken.builder()
                .token(token)
                .username(username)
                .expirationDateTime(LocalDateTime.now().plusSeconds(7200L))
                .build();
        validateTokenRequest = ValidateTokenRequest.builder()
                .token(token)
                .build();
        confirmPasswordResetRequest = ConfirmPasswordResetRequest.builder()
                .password(password)
                .token(token)
                .build();
    }

    @Test
    public void testValidResetToken() {
        when(userTokenService.validateTokenExpiration(token)).thenReturn(userToken);
        when(userService.getUserByUsername(username)).thenReturn(user);
        ValidateTokenResponse response = resetPasswordService.validate(validateTokenRequest);
        Assert.assertTrue(response.isValid());
    }

    @Test
    public void testInvalidResetToken() {
        when(userTokenService.validateTokenExpiration(token)).thenReturn(null);
        ValidateTokenResponse response = resetPasswordService.validate(validateTokenRequest);
        Assert.assertFalse(response.isValid());
    }

    @Test
    public void testConfirmationNoValidToken() {
        when(userTokenService.validateTokenExpiration(confirmPasswordResetRequest.getToken())).thenReturn(null);
        ConfirmPasswordResetResponse response = resetPasswordService.confirm(confirmPasswordResetRequest);
        Assert.assertFalse(response.isValid());
        Assert.assertEquals(LiteralConstants.PASSWORD_RESET_LINK_NOT_VALID, response.getMessage());
    }

    @Test
    public void testConfirmPasswordResetUserNotFound() {
        when(userTokenService.validateTokenExpiration(confirmPasswordResetRequest.getToken())).thenReturn(userToken);
        when(userService.getUserByUsername(username)).thenReturn(null);
        ConfirmPasswordResetResponse response = resetPasswordService.confirm(confirmPasswordResetRequest);
        Assert.assertNull(response);
    }

    @Test
    public void testConfirmPasswordResetUserFound() {
        when(userTokenService.validateTokenExpiration(confirmPasswordResetRequest.getToken())).thenReturn(userToken);
        when(userService.getUserByUsername(username)).thenReturn(user);
        when(userService.setOrUpdatePassword(user.getDomainAccount().getEmail(), confirmPasswordResetRequest.getPassword())).thenReturn(user);
        when(userService.getEmailUserDetails(user.getDomainAccount(), null)).thenReturn(emailUser);
        ConfirmPasswordResetResponse response = resetPasswordService.confirm(confirmPasswordResetRequest);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isValid());
        Assert.assertEquals(emailUser.getEmail(), response.getEmail());
    }

}