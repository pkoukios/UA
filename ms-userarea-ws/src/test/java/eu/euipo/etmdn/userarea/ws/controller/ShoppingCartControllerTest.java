/*
 * $Id:: ShoppingCartControllerTest.java 2021/05/12 04:38 dvelegra
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

import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationResponse;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchCriteriaShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchShoppingCartSort;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;
import eu.euipo.etmdn.userarea.ws.controller.shoppingcart.ShoppingCartController;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.SearchCriteriaShoppingCartResource;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.ShoppingCartSearchResource;
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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ShoppingCartControllerTest {

    @InjectMocks
    private ShoppingCartController shoppingCartController;

    @Mock
    private ShoppingCartService shoppingCartService;

    private Authentication authentication;
    private final Set<String> roles = new HashSet<>();
    private SearchCriteriaShoppingCartResource searchCriteriaShoppingCartResource;
    private SearchCriteriaShoppingCart searchCriteriaShoppingCart;
    private ShoppingCartSearch shoppingCartSearch;

    private static final String USERNAME = "test@xyz.com";
    private static final String APPLICATION_ID = "123";
    private static final String APPLICATION_NUMBER = "EFEM2021000001";

    @Before
    public void setUp() {
        shoppingCartController = new ShoppingCartController(shoppingCartService);
        Set<GrantedAuthority> authorities = new HashSet<>(AuthorityUtils.createAuthorityList("ROLE_TRADEMARKS", "ROLE_PAYMENTS"));
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, Collections.singletonMap("name", USERNAME), "name");
        authentication = new OAuth2AuthenticationToken(oAuth2User, authorities, "userarea");
        searchCriteriaShoppingCartResource = new SearchCriteriaShoppingCartResource();
        searchCriteriaShoppingCartResource.setSort(SearchShoppingCartSort.LAST_MODIFIED_BY);
        searchCriteriaShoppingCartResource.setSortType("ASC");
        searchCriteriaShoppingCart = new SearchCriteriaShoppingCart();
        searchCriteriaShoppingCart.setSort(SearchShoppingCartSort.LAST_MODIFIED_BY);
        searchCriteriaShoppingCart.setSortType("ASC");
        shoppingCartSearch  = new ShoppingCartSearch();
        ShoppingCartApplication shoppingCartApplication = new ShoppingCartApplication();
        shoppingCartApplication.setApplicationId(Long.valueOf(APPLICATION_ID));
        shoppingCartApplication.setNumber(APPLICATION_NUMBER);
        shoppingCartSearch.setContent(Collections.singletonList(shoppingCartApplication));
        roles.addAll(Arrays.asList("ROLE_TRADEMARKS", "ROLE_PAYMENTS"));
    }

    @Test
    public void testGetShoppingCartDetails() {
        when(shoppingCartService.getApplications(authentication.getName(), searchCriteriaShoppingCart, roles)).thenReturn(shoppingCartSearch);
        ResponseEntity<ShoppingCartSearchResource> result = shoppingCartController.getShoppingCart(authentication, searchCriteriaShoppingCartResource);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ShoppingCartSearchResource response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testModifyApplicationFromShoppingCart() {
        when(shoppingCartService.modifyApplication(authentication.getName(), APPLICATION_ID, false, false)).thenReturn("resumeUrl");
        ResponseEntity<ApplicationResponse> result = shoppingCartController.modifyShoppingCart(authentication, APPLICATION_ID);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        ApplicationResponse response = result.getBody();
        assertNotNull(response);
    }

    @Test
    public void testDeleteApplicationFromShoppingCart() {
        when(shoppingCartService.modifyApplication(authentication.getName(), APPLICATION_ID, true, false)).thenReturn("");
        ResponseEntity<Void> result = shoppingCartController.deleteFromShoppingCart(authentication, APPLICATION_ID);
        assertNotNull(result);
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }
}