/*
 * $Id:: AccountControllerTest.java 2021/03/01 09:07 dvelegra
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
import eu.euipo.etmdn.userarea.common.business.service.UserProfileService;
import eu.euipo.etmdn.userarea.common.domain.AccountDetails;
import eu.euipo.etmdn.userarea.common.domain.AccountDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.AccountTypeDetails;
import eu.euipo.etmdn.userarea.common.domain.AddressType;
import eu.euipo.etmdn.userarea.common.domain.ApplicantType;
import eu.euipo.etmdn.userarea.common.domain.CorrespondentAddress;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DomainAddress;
import eu.euipo.etmdn.userarea.common.domain.DomainApplication;
import eu.euipo.etmdn.userarea.common.domain.LegalAddress;
import eu.euipo.etmdn.userarea.common.domain.LockChildRequest;
import eu.euipo.etmdn.userarea.common.domain.MainAccountDetails;
import eu.euipo.etmdn.userarea.common.domain.MainAccountType;
import eu.euipo.etmdn.userarea.common.domain.PersonDetails;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AccountService accountService;
    @Mock
    private UserProfileService userProfileService;


    private DomainAccount domainAccount;
    private DomainAddress correspondentAddress;
    private CorrespondentAddress corrAddress;
    private final List<DomainAddress> addressList = new ArrayList<>();
    private AccountDetails accountDetails;
    private AccountRequest accountRequest;
    private AccountResponse accountResponse;
    private AccountDetailsRequest accountDetailsRequest;
    private AccountResponse deactivateAccountResponseInvalid;
    private AccountResponse deactivateAccountResponseValid;
    private Authentication authentication;

    private static final String username = "abc@xyz.com";
    private static final String password = "12345";
    private static final String language = "el";

    @Before
    public void setUp() {
        accountController = new AccountController(accountService, userProfileService);
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        accountRequest = AccountRequest.builder().userName(username).language(language).corrEmailNotification(false).build();
        DomainAddress legalAddress = DomainAddress.builder().streetName("Via Victoria").streetNumber("10")
                .city("Alicante").addressType(AddressType.LEGAL.getValue()).build();
        correspondentAddress = DomainAddress.builder().streetName("Via Mare").streetNumber("20")
                .city("Valencia").addressType(AddressType.CORRESPONDENT.getValue()).build();
        corrAddress = CorrespondentAddress.builder().streetName("Via Levant").streetNumber("30")
                .city("Barcelona").addressType(AddressType.CORRESPONDENT.getValue()).build();
        addressList.add(legalAddress);
        domainAccount = DomainAccount.builder()
                .id(1L).username(username).firstName("John").surName("Cool").verified(true).language("el")
                .applications(Collections.singletonList(DomainApplication.builder().id(1L).foModule("Trademark").build()))
                .addresses(addressList)
                .roles("ROLE_TRADEMARKS")
                .corrEmailNotification(true)
                .build();
        accountDetails = AccountDetails.builder()
                .accountTypeDetails(AccountTypeDetails.builder().personType(MainAccountType.APPLICANT.getValue()).accountType(ApplicantType.INDIVIDUAL.getValue()).build())
                .mainAccountDetails(MainAccountDetails.builder().firstName("John").surName("Cool").build())
                .legalAddress(LegalAddress.builder().streetName("Via Victoria").city("Alicante").streetNumber("10").build())
                .build();
        accountDetailsRequest = AccountDetailsRequest.builder()
                .personDetails(PersonDetails.builder().firstName("John").surName("Cool").nationality("GR").build())
                .legalAddress(LegalAddress.builder().streetName(legalAddress.getStreetName()).city(legalAddress.getCity()).country("es").build())
                .build();
        accountResponse = AccountResponse.builder()
                .userName(username)
                .build();
        deactivateAccountResponseValid = AccountResponse.builder()
                .userName(username)
                .valid(Boolean.TRUE)
                .build();
        deactivateAccountResponseInvalid = AccountResponse.builder()
                .userName(username)
                .valid(Boolean.FALSE)
                .build();
    }

    @Test
    public void testGetAccountDetails() {
        when(accountService.getAccountByUsername(username)).thenReturn(domainAccount);
        when(accountService.getAccountByUsername(domainAccount.getMainAccountName())).thenReturn(domainAccount);
        ResponseEntity<?> result = accountController.getAccountDetails(authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountDetails response = (AccountDetails) result.getBody();
        assertNotNull(response);
        assertEquals(domainAccount.getMainAccountType(), response.getAccountTypeDetails().getAccountType());
        assertEquals(domainAccount.getLegalType(), response.getAccountTypeDetails().getPersonType());
        assertEquals(domainAccount.getFirstName(), response.getMainAccountDetails().getFirstName());
        assertEquals(domainAccount.getSurName(), response.getMainAccountDetails().getSurName());
        assertEquals(domainAccount.getAddresses().get(0).getStreetName(), response.getLegalAddress().getStreetName());
        assertEquals(domainAccount.getAddresses().get(0).getCity(), response.getLegalAddress().getCity());
    }

    @Test
    public void testGetDetailsExistingAccountAllAddresses() {
        accountDetails.setCorrespondentAddress(corrAddress);
        addressList.add(correspondentAddress);
        domainAccount.setCorrAddress(true);
        domainAccount.setAddresses(addressList);
        when(accountService.getAccountByUsername(username)).thenReturn(domainAccount);
        when(accountService.getAccountByUsername(domainAccount.getMainAccountName())).thenReturn(domainAccount);
        ResponseEntity<?> result = accountController.getAccountDetails(authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountDetails response = (AccountDetails) result.getBody();
        assertNotNull(response);
        assertEquals(domainAccount.getMainAccountType(), response.getAccountTypeDetails().getAccountType());
        assertEquals(domainAccount.getLegalType(), response.getAccountTypeDetails().getPersonType());
        assertEquals(domainAccount.getFirstName(), response.getMainAccountDetails().getFirstName());
        assertEquals(domainAccount.getSurName(), response.getMainAccountDetails().getSurName());
        assertEquals(domainAccount.getAddresses().get(0).getStreetName(), response.getLegalAddress().getStreetName());
        assertEquals(domainAccount.getAddresses().get(0).getCity(), response.getLegalAddress().getCity());
        assertEquals(domainAccount.getAddresses().get(1).getStreetName(), response.getCorrespondentAddress().getStreetName());
        assertEquals(domainAccount.getAddresses().get(1).getCity(), response.getCorrespondentAddress().getCity());
    }

    @Test
    public void testGetAccountDetailsForNotExistingAccount() {
        when(authentication.getName()).thenReturn(null);
        ResponseEntity<?> result = accountController.getAccountDetails(authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testChangeAccountLanguage() {
        accountResponse.setLanguage(language);
        when(accountService.changeLanguage(accountRequest)).thenReturn(accountResponse);
        ResponseEntity<?> result = accountController.changeLanguage(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = (AccountResponse) result.getBody();
        assertNotNull(response);
        assertEquals(domainAccount.getLanguage(), response.getLanguage());
    }

    @Test
    public void testChangeLanguageForNotExistingAccount() {
        when(accountService.changeLanguage(accountRequest)).thenReturn(null);
        ResponseEntity<?> result = accountController.changeLanguage(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testChangeEmailNotificationFlag() {
        when(accountService.modifyEmailNotification(accountRequest)).thenReturn(accountResponse);
        ResponseEntity<?> result = accountController.modifyEmailNotification(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = (AccountResponse) result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testChangeEmailNotificationFlagForNotExistingAccount() {
        when(accountService.modifyEmailNotification(accountRequest)).thenReturn(null);
        ResponseEntity<?> result = accountController.modifyEmailNotification(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testUpdateAccountDetails() {
        when(accountService.getMainAccount(username)).thenReturn(domainAccount);
        when(accountService.updateDetails(domainAccount, accountDetailsRequest,username)).thenReturn(accountResponse);
        ResponseEntity<?> result = accountController.updateAccountDetails(accountDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = (AccountResponse) result.getBody();
        assertNotNull(response);
        assertEquals(domainAccount.getUsername(), response.getUserName());
    }

    @Test
    public void testUpdateAccountDetailsWithCorrespondentAddress() {
        accountDetails.setCorrespondentAddress(corrAddress);
        addressList.add(correspondentAddress);
        domainAccount.setCorrAddress(true);
        domainAccount.setAddresses(addressList);
        when(accountService.getMainAccount(username)).thenReturn(domainAccount);
        when(accountService.updateDetails(domainAccount, accountDetailsRequest,username)).thenReturn(accountResponse);
        ResponseEntity<?> result = accountController.updateAccountDetails(accountDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = (AccountResponse) result.getBody();
        assertNotNull(response);
        assertEquals(domainAccount.getUsername(), response.getUserName());
    }

    @Test
    public void testUpdateDetailsForNotExistingAccount() {
        ResponseEntity<?> result = accountController.updateAccountDetails(accountDetailsRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testChangeAccountPasswordSuccess() {
        accountRequest.setOldPassword(password);
        accountRequest.setNewPassword("azws03E");
        accountResponse.setValid(true);
        accountResponse.setMessage(LiteralConstants.PASSWORD_CHANGE_SUCCESS);
        when(userProfileService.changePassword(accountRequest)).thenReturn(accountResponse);
        ResponseEntity<?> result = accountController.changePassword(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        AccountResponse response = (AccountResponse) result.getBody();
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(LiteralConstants.PASSWORD_CHANGE_SUCCESS, response.getMessage());
    }

    @Test
    public void testChangeAccountPasswordPasswordNotMatch() {
        accountRequest.setOldPassword("123");
        accountRequest.setNewPassword("azws03E");
        accountResponse.setValid(false);
        accountResponse.setMessage(LiteralConstants.PASSWORD_NOT_MATCH);
        when(userProfileService.changePassword(accountRequest)).thenReturn(accountResponse);
        ResponseEntity<?> result = accountController.changePassword(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        AccountResponse response = (AccountResponse) result.getBody();
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals(LiteralConstants.PASSWORD_NOT_MATCH, response.getMessage());
    }

    @Test
    public void testChangePasswordForNotExistingAccount() {
        accountRequest.setOldPassword(password);
        accountRequest.setNewPassword("azws03E");
        when(userProfileService.changePassword(accountRequest)).thenReturn(null);
        ResponseEntity<AccountResponse> result = accountController.changePassword(accountRequest, authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testAccountDeactivationIsInvalid() {
        when(accountService.validateDeactivation(username)).thenReturn(deactivateAccountResponseInvalid);
        ResponseEntity<AccountResponse> result = accountController.validateDeactivation(authentication);
        AccountResponse response = result.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assert response != null;
        assertFalse(response.isValid());
    }

    @Test
    public void testAccountDeactivationReturnsNull() {
        when(accountService.validateDeactivation(username)).thenReturn(null);
        ResponseEntity<AccountResponse> result = accountController.validateDeactivation(authentication);
        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void testAccountDeactivationIsValid() {
        when(accountService.validateDeactivation(username)).thenReturn(deactivateAccountResponseValid);
        ResponseEntity<AccountResponse> result = accountController.validateDeactivation(authentication);
        AccountResponse response = result.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assert response != null;
        assertTrue(response.isValid());
    }

    @Test
    public void testAccountDeactivationConfirmationSuccess() {
        when(accountService.deactivate(username)).thenReturn(deactivateAccountResponseValid);
        ResponseEntity<AccountResponse> result = accountController.confirmDeactivation(authentication);
        AccountResponse response = result.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assert response != null;
        assertTrue(response.isValid());
    }

    @Test
    public void testAccountDeactivationConfirmationFailWhenRequestInvalid() {
        when(accountService.deactivate(username)).thenReturn(deactivateAccountResponseInvalid);
        ResponseEntity<AccountResponse> result = accountController.confirmDeactivation(authentication);
        AccountResponse response = result.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assert response != null;
        assertFalse(response.isValid());
    }

    @Test
    public void testAccountDeactivationConfirmationFail() {
        when(accountService.deactivate(username)).thenReturn(null);
        ResponseEntity<AccountResponse> result = accountController.confirmDeactivation(authentication);
        AccountResponse response = result.getBody();
        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
        assertNull(response);
    }

    @Test
    public void testLockAccount() {
        AccountResponse response = new AccountResponse();
        response.setLocked(true);
        when(accountService.lockAccount(username,null)).thenReturn(response);
        ResponseEntity<AccountResponse> result = accountController.lockAccount(authentication, new LockChildRequest());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

}
