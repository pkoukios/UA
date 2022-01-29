/*
 * $Id:: AuthControllerTest.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.business.service.AuthenticationService;
import eu.euipo.etmdn.userarea.common.domain.AuthResponse;
import eu.euipo.etmdn.userarea.common.domain.LoginDetails;
import eu.euipo.etmdn.userarea.common.domain.LoginRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private AccountService accountService;

    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private LoginDetails loginDetails;
    private Authentication authentication;

    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";
    private static final String password = "12345";

    @Before
    public void setUp() {
        authController = new AuthController(authenticationService, accountService);
        loginRequest = LoginRequest.builder().username(username).password(password).build();
        authResponse = AuthResponse.builder().build();
        loginDetails = LoginDetails.builder().email(email).language("el").fullName("John Cool").build();
        Set<GrantedAuthority> authorities = new HashSet<>(AuthorityUtils.createAuthorityList("ROLE_TRADEMARKS"));
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Collections.singletonMap("name", username), "name");
        authentication = new OAuth2AuthenticationToken(oAuth2User, authorities, "userarea");
    }

    @Test
    public void testSuccessLogin()  {
        when(authenticationService.login(loginRequest)).thenReturn(authResponse);
        ResponseEntity<AuthResponse> result = authController.login(loginRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AuthResponse response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testFailedLogin()  {
        when(authenticationService.login(loginRequest)).thenReturn(null);
        ResponseEntity<AuthResponse> result = authController.login(loginRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        AuthResponse response = result.getBody();
        assertNull(response);
    }

   @Test
    public void testSuccessGetLoggedInUserDetails()  {
        when(accountService.getProfile(username)).thenReturn(loginDetails);
        ResponseEntity<LoginDetails> result = authController.getLoggedInUserDetails(authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        LoginDetails response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testFailedGetLoggedInUserDetails()  {
        ResponseEntity<LoginDetails> result = authController.getLoggedInUserDetails(authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        LoginDetails response = result.getBody();
        assertNull(response);
    }
}