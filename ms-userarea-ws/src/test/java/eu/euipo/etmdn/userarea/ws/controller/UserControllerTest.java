/*
 * $Id:: UserControllerTest.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.AccountEmailNotificationService;
import eu.euipo.etmdn.userarea.common.business.service.RegistrationService;
import eu.euipo.etmdn.userarea.common.business.service.ResetPasswordService;
import eu.euipo.etmdn.userarea.common.business.service.impl.EmailServiceImpl;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetResponse;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.ResetPasswordRequest;
import eu.euipo.etmdn.userarea.common.domain.ResetPasswordResponse;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenRequest;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenResponse;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.email.EmailUser;
import eu.euipo.etmdn.userarea.common.domain.exception.IllegalPasswordException;
import eu.euipo.etmdn.userarea.common.domain.exception.IllegalUsernameException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.PASSWORD_RESET_LINK_NOT_VALID;
import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.REGISTRATION_MESSAGE;
import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.REGISTRATION_TOKEN_NOT_VALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private EmailServiceImpl emailService;
    @Mock
    private AccountEmailNotificationService accountEmailNotificationService;
    @Mock
    private RegistrationService registrationService;
    @Mock
    private ResetPasswordService resetPasswordService;

    private EmailUser emailUser;
    private ValidateTokenRequest validateTokenRequest;
    private ConfirmPasswordResetRequest confirmPasswordResetRequest;
    private AccountRegistrationRequest accountRegistrationRequest;
    private ConfirmRegistrationRequest confirmRegistrationRequest;
    private ConfirmRegistrationResponse confirmRegistrationResponse;
    private ResetPasswordRequest resetPasswordRequest;

    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";
    private static final String token = "aaa111";
    private static final String password = "12345";

    @Before
    public void setUp() {
        userController = new UserController(registrationService, resetPasswordService, accountEmailNotificationService);
        emailUser = EmailUser.builder().userName(username).emailName("ABC DEF").token("12345").email(email).build();
        resetPasswordRequest = ResetPasswordRequest.builder().email(email).build();
        validateTokenRequest = ValidateTokenRequest.builder().token(token).build();
        confirmPasswordResetRequest = ConfirmPasswordResetRequest.builder().token(token).password(password).build();
        accountRegistrationRequest = AccountRegistrationRequest.builder()
                .userName(username)
                .password(password)
                .email(email)
                .language(Locale.getDefault().getLanguage())
                .build();
        confirmRegistrationResponse = ConfirmRegistrationResponse.builder()
                .userName(username)
                .build();
        confirmRegistrationRequest = ConfirmRegistrationRequest.builder()
                .token(token)
                .build();
    }

    @Test
    public void testSuccessResetPassword() {
        when(accountEmailNotificationService.sendResetPasswordEmail(resetPasswordRequest.getEmail())).thenReturn(ResetPasswordResponse.builder()
                .email(resetPasswordRequest.getEmail())
                .message(LiteralConstants.RESET_PASSWORD)
                .build());
        ResponseEntity<ResetPasswordResponse> result = userController.resetPassword(resetPasswordRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ResetPasswordResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals(LiteralConstants.RESET_PASSWORD, response.getMessage());
    }

    @Test
    public void testResetPasswordUnknownUser() {
        when(accountEmailNotificationService.sendResetPasswordEmail(resetPasswordRequest.getEmail())).thenReturn(ResetPasswordResponse.builder()
                .email(resetPasswordRequest.getEmail())
                .message(LiteralConstants.RESET_PASSWORD)
                .build());
        ResponseEntity<ResetPasswordResponse> result = userController.resetPassword(resetPasswordRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ResetPasswordResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(emailUser.getEmail(), response.getEmail());
        assertEquals(LiteralConstants.RESET_PASSWORD, response.getMessage());
    }

    @Test
    public void testValidateTokenSuccess() {
        when(resetPasswordService.validate(validateTokenRequest)).thenReturn(ValidateTokenResponse.builder()
                .email(email)
                .valid(Boolean.TRUE)
                .build());
        ResponseEntity<ValidateTokenResponse> result = userController.validateToken(validateTokenRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ValidateTokenResponse response = result.getBody();
        assertNotNull(response);
        assertTrue(response.isValid());
    }

    @Test
    public void testFailedValidatedToken() {
        when(resetPasswordService.validate(validateTokenRequest)).thenReturn(ValidateTokenResponse.builder()
                .email(email)
                .message("password.reset.link.not.valid")
                .valid(Boolean.FALSE)
                .build());
        ResponseEntity<ValidateTokenResponse> result = userController.validateToken(validateTokenRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ValidateTokenResponse response = result.getBody();
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals(PASSWORD_RESET_LINK_NOT_VALID, response.getMessage());
    }

    @Test
    public void testConfirmPasswordResetSuccess() {
        when(resetPasswordService.confirm(confirmPasswordResetRequest))
                .thenReturn(ConfirmPasswordResetResponse.builder().valid(Boolean.TRUE).build());
        ResponseEntity<ConfirmPasswordResetResponse> result = userController.confirmPasswordReset(confirmPasswordResetRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ConfirmPasswordResetResponse response = result.getBody();
        assertNotNull(response);
        assertTrue(response.isValid());
    }

    @Test
    public void testConfirmPasswordResetFailNotValidToken() {
        when(resetPasswordService.confirm(confirmPasswordResetRequest)).thenReturn(ConfirmPasswordResetResponse.builder()
                .valid(Boolean.FALSE).message(PASSWORD_RESET_LINK_NOT_VALID).build());
        ResponseEntity<ConfirmPasswordResetResponse> result = userController.confirmPasswordReset(confirmPasswordResetRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ConfirmPasswordResetResponse response = result.getBody();
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals(PASSWORD_RESET_LINK_NOT_VALID, response.getMessage());
        verify(emailService, times(0)).sendConfirmPasswordMessageEmail(any(EmailUser.class));
    }

    @Test
    public void testConfirmPasswordResetFailNotValidUser() {
        when(resetPasswordService.confirm(confirmPasswordResetRequest)).thenReturn(null);
        ResponseEntity<ConfirmPasswordResetResponse> result = userController.confirmPasswordReset(confirmPasswordResetRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(emailService, times(0)).sendConfirmPasswordMessageEmail(any(EmailUser.class));
    }

    @Test
    public void testRegisterForValidationSuccess()  {
        doNothing().when(registrationService).validate(accountRegistrationRequest.getUserName(),
                accountRegistrationRequest.getEmail(), accountRegistrationRequest.getPassword());
        ResponseEntity<AccountRegistrationResponse> result = userController.registerForValidation(accountRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountRegistrationResponse response =  result.getBody();
        assertNotNull(response);
        assertEquals(username, response.getUserName());
    }

    @Test(expected = IllegalUsernameException.class)
    public void testRegisterForValidationFailIllegalUsername()  {
        doThrow(IllegalUsernameException.class).when(registrationService).validate(accountRegistrationRequest.getUserName(),
                accountRegistrationRequest.getEmail(), accountRegistrationRequest.getPassword());
        ResponseEntity<AccountRegistrationResponse> result = userController.registerForValidation(accountRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test(expected = IllegalPasswordException.class)
    public void testRegisterForValidationFailIllegalPassword() {
        doThrow(IllegalPasswordException.class).when(registrationService)
                .validate(accountRegistrationRequest.getUserName(), accountRegistrationRequest.getEmail(), accountRegistrationRequest.getPassword());
        ResponseEntity<AccountRegistrationResponse> result = userController.registerForValidation(accountRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testRegisterUserAccountCreationSuccess() {
        when(registrationService.register(any(AccountRegistrationRequest.class), any(DomainAccount.class))).thenReturn(emailUser);
        ResponseEntity<AccountRegistrationResponse> result = userController.userAccountRegistration(accountRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(username, response.getUserName());
        assertEquals(REGISTRATION_MESSAGE, response.getMessage());
    }

    @Test
    public void testRegisterAccountConfirmationSuccess() {
        confirmRegistrationResponse.setValid(true);
        when(registrationService.confirm(confirmRegistrationRequest)).thenReturn(confirmRegistrationResponse);
        ResponseEntity<ConfirmRegistrationResponse> result = userController.confirmRegistration(confirmRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ConfirmRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(username, response.getUserName());
        assertTrue(response.isValid());
    }

    @Test
    public void testRegisterAccountConfirmationFailNotValidToken() {
        confirmRegistrationResponse.setValid(false);
        confirmRegistrationResponse.setMessage(REGISTRATION_TOKEN_NOT_VALID);
        when(registrationService.confirm(confirmRegistrationRequest)).thenReturn(confirmRegistrationResponse);
        ResponseEntity<ConfirmRegistrationResponse> result = userController.confirmRegistration(confirmRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ConfirmRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(REGISTRATION_TOKEN_NOT_VALID, response.getMessage());
        assertFalse(response.isValid());
    }

    @Test
    public void testResendEmailForRegistrationSuccessToken() {
        confirmRegistrationResponse.setValid(true);
        when(registrationService.resendToken(confirmRegistrationRequest)).thenReturn(confirmRegistrationResponse);
        ResponseEntity<ConfirmRegistrationResponse> result = userController.resendTokenForRegistration(confirmRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ConfirmRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(username, response.getUserName());
        assertTrue(response.isValid());
    }

    @Test
    public void testResendEmailForRegistrationSuccessUsername() {
        ConfirmRegistrationRequest request = ConfirmRegistrationRequest.builder().userName(username).build();
        confirmRegistrationResponse.setValid(true);
        when(registrationService.resendToken(request)).thenReturn(confirmRegistrationResponse);
        ResponseEntity<ConfirmRegistrationResponse> result = userController.resendTokenForRegistration(request);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ConfirmRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(username, response.getUserName());
        assertTrue(response.isValid());
    }

    @Test
    public void testResendEmailForRegistrationFailForToken() {
        ResponseEntity<ConfirmRegistrationResponse> result = userController.resendTokenForRegistration(confirmRegistrationRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        ConfirmRegistrationResponse response = result.getBody();
        assertNull(response);
        verify(emailService, times(0)).sendRegistrationMessageEmail(any(EmailUser.class));
    }
}
