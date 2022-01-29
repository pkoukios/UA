/*
 * $Id:: ChildControllerTest.java 2021/03/01 09:07 dvelegra
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
import eu.euipo.etmdn.userarea.common.business.service.ChildService;
import eu.euipo.etmdn.userarea.common.business.service.RegistrationService;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.AccountRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.ChildDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.ChildInformation;
import eu.euipo.etmdn.userarea.common.domain.ChildUpdateDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class ChildControllerTest {


    @InjectMocks
    private ChildController childController;

    @Mock
    private ChildService childService;
    @Mock
    private RegistrationService registrationService;
    @Mock
    private AccountService accountService;

    private ChildInformation childInformation;
    private ChildUpdateDetailsRequest childUpdateDetailsRequest;
    private ChildDetailsRequest childDetailsRequest;
    private AccountRegistrationRequest accountRegistrationRequest;
    private AccountRegistrationResponse accountRegistrationResponse;
    private AccountResponse accountResponse;
    private Authentication authentication;
    private AccountRequest deactivateAccountRequest;

    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";

    @Before
    public void setUp() {
        childController = new ChildController(childService, registrationService, accountService);
        childInformation = ChildInformation.builder().firstName("John").lastName("Cool").email(email).status("Active").build();
        accountRegistrationRequest = AccountRegistrationRequest.builder()
                .userName(username)
                .email(email)
                .language(Locale.getDefault().getLanguage())
                .build();
        accountRegistrationResponse = AccountRegistrationResponse.builder()
                .userName(username)
                .build();
        childDetailsRequest = ChildDetailsRequest.builder().email(username).firstName("John").lastName("Cool").build();
        childUpdateDetailsRequest = ChildUpdateDetailsRequest.builder().userName(username).firstName("John").lastName("Cool").build();
        accountResponse = AccountResponse.builder().userName(username).build();
        deactivateAccountRequest = AccountRequest.builder().userName(username).build();
        Set<GrantedAuthority> authorities = new HashSet<>(AuthorityUtils.createAuthorityList("ROLE_TRADEMARKS"));
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Collections.singletonMap("name", username), "name");
        authentication = new OAuth2AuthenticationToken(oAuth2User, authorities, "userarea");
    }

    @Test
    public void testChildRegistrationSuccess()  {
        when(registrationService.registerChild(childDetailsRequest, username)).thenReturn(accountRegistrationResponse);
        ResponseEntity<AccountRegistrationResponse> result = childController.register(childDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(accountRegistrationRequest.getUserName(), response.getUserName());
    }

    @Test
    public void testChildRegistrationBadRequest()  {
        accountRegistrationResponse.setMessage(LiteralConstants.ENABLE_IP_RIGHT_ACCESS);
        when(registrationService.registerChild(childDetailsRequest, username)).thenReturn(accountRegistrationResponse);
        ResponseEntity<AccountRegistrationResponse> result = childController.register(childDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        AccountRegistrationResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(accountRegistrationRequest.getUserName(), response.getUserName());
        assertEquals(LiteralConstants.ENABLE_IP_RIGHT_ACCESS, response.getMessage());
    }

    @Test
    public void testChildRegistrationNotFound()  {
        when(registrationService.registerChild(childDetailsRequest, username)).thenReturn(null);
        ResponseEntity<AccountRegistrationResponse> result = childController.register(childDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        AccountRegistrationResponse response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testPagedChildAccounts() {
        Integer page= 0;
        Integer size = 1;
        String sortColumn = "firstName";
        String sortType = "DESC";
        Page<ChildInformation> pageableChild = new PageImpl<>(Collections.singletonList(childInformation));
        when(childService.getChildAccounts(username, page, size, sortColumn, sortType)).thenReturn(pageableChild);
        ResponseEntity<Page<ChildInformation>> result = childController.getChildAccountsDetails(page, size, sortColumn, sortType, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Page<ChildInformation> response = result.getBody();
        assertNotNull(response);
        assertEquals(1, response.getNumberOfElements());
    }

    @Test
    public void testPagedChildAccountsUnknownUsername() {
        Integer page= 0;
        Integer size = 1;
        String sortColumn = "firstName";
        String sortType = "DESC";
        when(childService.getChildAccounts(username, page, size, sortColumn, sortType)).thenReturn(null);
        ResponseEntity<Page<ChildInformation>> result = childController.getChildAccountsDetails(page, size, sortColumn, sortType, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        Page<ChildInformation> response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testChildUpdateSuccess()  {
        when(childService.update(childUpdateDetailsRequest, username)).thenReturn(accountResponse);
        ResponseEntity<AccountResponse> result = childController.update(childUpdateDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(accountResponse.getUserName(), response.getUserName());
    }

    @Test
    public void testChildUpdateBadRequest()  {
        accountResponse.setMessage(LiteralConstants.ENABLE_IP_RIGHT_ACCESS);
        when(childService.update(childUpdateDetailsRequest, username)).thenReturn(accountResponse);
        ResponseEntity<AccountResponse> result = childController.update(childUpdateDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(accountResponse.getUserName(), response.getUserName());
        assertEquals(LiteralConstants.ENABLE_IP_RIGHT_ACCESS, response.getMessage());
    }

    @Test
    public void testChildNotFound()  {
        when(childService.update(childUpdateDetailsRequest, username)).thenReturn(null);
        ResponseEntity<AccountResponse> result = childController.update(childUpdateDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testChildActivateSuccess()  {
        when(accountService.activate(username)).thenReturn(username);
        ResponseEntity<AccountResponse> result = childController.activate(deactivateAccountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(accountResponse.getUserName(), response.getUserName());
    }

    @Test
    public void testChildDeActivateSuccess()  {
        when(accountService.deactivate(username)).thenReturn(accountResponse);
        ResponseEntity<AccountResponse> result = childController.deactivate(deactivateAccountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNotNull(response);
        assertEquals(accountResponse.getUserName(), response.getUserName());
    }

    @Test
    public void testChildActivationNotFound()  {
        ResponseEntity<AccountResponse> result = childController.activate(deactivateAccountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNull(response);
    }

    @Test
    public void testChildDeactivateNotFound()  {
        ResponseEntity<AccountResponse> result = childController.deactivate(deactivateAccountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        AccountResponse response = result.getBody();
        assertNull(response);
    }
}
