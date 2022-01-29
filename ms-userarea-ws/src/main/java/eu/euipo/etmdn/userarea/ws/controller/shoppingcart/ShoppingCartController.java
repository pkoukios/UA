/*
 * $Id:: ShoppingCartController.java 2021/04/17 04:18 tantonop
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.ws.controller.shoppingcart;

import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationResponse;
import eu.euipo.etmdn.userarea.domain.shoppingcart.SearchCriteriaShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartSearch;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.SearchCriteriaShoppingCartResource;
import eu.euipo.etmdn.userarea.ws.domain.shoppingcart.ShoppingCartSearchResource;
import eu.euipo.etmdn.userarea.ws.mapper.shoppingcart.SearchCriteriaShoppingCartMapper;
import eu.euipo.etmdn.userarea.ws.mapper.shoppingcart.ShoppingCartSearchMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/shoppingcarts")
@AllArgsConstructor
public class ShoppingCartController {

    private ShoppingCartService shoppingCartService;

    /**
     * returns the shopping cart
     * @param authentication the authenticated user
     * @param searchCriteriaShoppingCartResource the search criteria for shopping cart
     * @return a list of shopping cart applications
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS') or hasRole('ROLE_ADMINISTRATOR')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShoppingCartSearchResource> getShoppingCart(Authentication authentication,@Valid @RequestBody SearchCriteriaShoppingCartResource searchCriteriaShoppingCartResource){
        log.info("Requesting shopping cart for user {}",authentication.getName());
        SearchCriteriaShoppingCart searchCriteriaShoppingCart = SearchCriteriaShoppingCartMapper.MAPPER.map(searchCriteriaShoppingCartResource);
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        ShoppingCartSearch shoppingCartSearch = shoppingCartService.getApplications(authentication.getName(), searchCriteriaShoppingCart, roles);
        return new ResponseEntity<>(ShoppingCartSearchMapper.MAPPER.map(shoppingCartSearch), HttpStatus.OK);
    }

    /**
     * Modifies an application from the application shopping cart
     * It will change the application status to draft and it will remove it from the shopping cart
     *
     * @param authentication the authentication
     * @param applicationId the application id
     * @return String the resume url if modification is successful
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS')")
    @PutMapping("/modify/{applicationId}")
    public ResponseEntity<ApplicationResponse> modifyShoppingCart(Authentication authentication, @PathVariable String applicationId){
        log.info("Request for user {} to modify application with id {} from shopping cart",authentication.getName(), applicationId);
        String resumeUrl = shoppingCartService.modifyApplication(authentication.getName(), applicationId,false, false);
        return ResponseEntity.ok(ApplicationResponse.builder().applicationNumber(applicationId).resumeUrl(resumeUrl).build());
    }

    /**
     * Delete an application from the application shopping cart
     * It will change the application status to draft and it will remove it from the shopping cart
     *
     * @param authentication the authentication
     * @param applicationId the application id
     * @return String an empty string
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS')")
    @DeleteMapping("/delete/{applicationId}")
    public ResponseEntity<Void> deleteFromShoppingCart(Authentication authentication, @PathVariable String applicationId){
        log.info("Request for user {} to delete application with id {} from shopping cart",authentication.getName(), applicationId);
        shoppingCartService.modifyApplication(authentication.getName(), applicationId,true, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
