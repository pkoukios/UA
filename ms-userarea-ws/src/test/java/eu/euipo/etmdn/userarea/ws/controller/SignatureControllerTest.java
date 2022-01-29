/*
 * $Id:: SignatureControllerTest.java 2021/05/07 05:53 dvelegra
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

package eu.euipo.etmdn.userarea.ws.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.SignatureService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationResponse;
import eu.euipo.etmdn.userarea.common.domain.signature.Signature;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.signature.SignatureSortingCriteriaRequest;
import eu.euipo.etmdn.userarea.ws.domain.signature.SignatureResource;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.TRADEMARK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class SignatureControllerTest {

    @InjectMocks
    private SignatureController signatureController;

    @Mock
    private SignatureService signatureService;

    private Signature signature;
    private Authentication authentication;
    private SignatureSortingCriteriaRequest signatureSortingCriteriaRequest;
    private final Set<String> roles = new HashSet<>();

    private static final String USERNAME = "abc@xyz.com";
    private static final String APPLICATION_NUMBER = "EFEM2021000001";

    @Before
    public void setUp() {
        signatureController = new SignatureController(signatureService);
        String name = "Paul Cage,John Cool,Jack Rabbit";
        String capacity = "Employee Representative,Employee Representative,Employee Representative";
        String date = "2021-02-11T15:24:17,2021-02-11T15:18:52,2020-10-02T16:29:18";
        signature = Signature.builder().type(ApplicationType.TRADEMARK.value)
                .number("123456")
                .name(name)
                .capacity(capacity)
                .date(date)
                .build();
        Set<GrantedAuthority> authorities = new HashSet<>(AuthorityUtils.createAuthorityList("ROLE_TRADEMARKS", "ROLE_SIGNATURES"));
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Collections.singletonMap("name", USERNAME), "name");
        authentication = new OAuth2AuthenticationToken(oAuth2User, authorities, "userarea");
        signatureSortingCriteriaRequest = new SignatureSortingCriteriaRequest();
        signatureSortingCriteriaRequest.setSortType("ASC");
        roles.addAll(Arrays.asList("ROLE_TRADEMARKS", "ROLE_SIGNATURES"));
    }

    @Test
    public void testSignatureDetails() {
        when(signatureService.getSignatures(authentication.getName(), roles, signatureSortingCriteriaRequest)).thenReturn(Collections.singletonList(signature));
        ResponseEntity<List<SignatureResource>> result = signatureController.getSignatures(authentication, signatureSortingCriteriaRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        List<SignatureResource> response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testDeleteApplicationInSignature() {
        when(signatureService.deleteApplication(authentication.getName(), APPLICATION_NUMBER)).thenReturn(StringUtils.EMPTY);
        ResponseEntity<Void> result = signatureController.deleteApplication(authentication, TRADEMARK, APPLICATION_NUMBER);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void testModifyApplicationInSignature() {
        when(signatureService.modifyApplication(authentication.getName(), APPLICATION_NUMBER)).thenReturn("resumeUrl");
        ResponseEntity<ApplicationResponse> result = signatureController.modifyApplication(authentication, TRADEMARK, APPLICATION_NUMBER);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApplicationResponse response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testDeleteSignatures() {
        String applicationId = "applicationId";
        doNothing().when(signatureService).deleteByApplicationNumber(authentication.getName(),applicationId);
        ResponseEntity<Void> result = signatureController.delete(authentication, applicationId);
        assertNotNull(result);
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }
}