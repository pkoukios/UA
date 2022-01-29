/*
 * $Id:: AuthController.java 2021/03/01 09:07 dvelegra
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Authentication controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AccountService accountService;

    /**
     * Instantiates Auth controller.
     *
     * @param authenticationService  the authentication service
     * @param accountService the account service
     */
    @Autowired
    public AuthController(final AuthenticationService authenticationService, AccountService accountService) {
        this.authenticationService = authenticationService;
        this.accountService = accountService;
    }

    /**
     * Login method.
     *
     * @param loginRequest the login request
     * @return {@link AuthResponse} the response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request for username={}", loginRequest.getUsername());
        AuthResponse response = authenticationService.login(loginRequest);
        if(response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieve logged in user details.
     *
     * @return {@link AuthResponse} the response
     */
    @GetMapping("/me")
    public ResponseEntity<LoginDetails> getLoggedInUserDetails(Authentication authentication) {
        LoginDetails loginDetails = accountService.getProfile(authentication.getName());
        if(loginDetails != null) {
            if(authentication.getAuthorities().stream().anyMatch(item->"ROLE_IMPERSONATOR".equalsIgnoreCase(item.getAuthority()))){
                loginDetails.setImpersonatedUser(true);
            }else{
                loginDetails.setImpersonatedUser(false);
            }
            return ResponseEntity.ok(loginDetails);
        }
        return ResponseEntity.notFound().build();
    }

}
