/*
 * $Id:: AccountController.java 2021/03/01 09:07 dvelegra
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
import eu.euipo.etmdn.userarea.common.business.utils.AccountUtils;
import eu.euipo.etmdn.userarea.common.domain.AccountDetails;
import eu.euipo.etmdn.userarea.common.domain.AccountDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.LockChildRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Account controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;
    private final UserProfileService userProfileService;

    /**
     * Instantiates Account controller.
     *
     * @param accountService             the account service
     * @param userProfileService         the user profile service
     */
    @Autowired
    public AccountController(final AccountService accountService, final UserProfileService userProfileService) {
        this.accountService = accountService;
        this.userProfileService = userProfileService;
    }

    /**
     * Get Account details.
     *
     * @return {@link AccountDetails} the AccountDetails response entity
     */
    @GetMapping("/details")
    public ResponseEntity<AccountDetails> getAccountDetails(Authentication authentication) {
        DomainAccount account = accountService.getAccountByUsername(authentication.getName());
        if (account != null) {
            log.info("Retrieve account details for username={}", account.getUsername());
            AccountDetails details = AccountUtils.buildAccountDetails(account, accountService.getAccountByUsername(account.getMainAccountName()));
            if(details != null) {
                if( details.getLoginDetails() != null) {
                    details.getLoginDetails().setImpersonatedUser(authentication.getAuthorities()
                            .stream().anyMatch(item -> "ROLE_IMPERSONATOR".equalsIgnoreCase(item.getAuthority())));
                }
                return ResponseEntity.ok(details);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Update user account details.
     *
     * @param accountDetailsRequest the account details to be updated
     * @return {@link AccountResponse} the account response entity
     */
    @PutMapping("/details")
    @PreAuthorize("hasRole('ROLE_ACCOUNT_ADMINISTRATOR') or @accountServiceImpl.isMainAccount(authentication.name) or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<AccountResponse> updateAccountDetails(@RequestBody AccountDetailsRequest accountDetailsRequest, Authentication authentication) {
        DomainAccount account = accountService.getMainAccount(authentication.getName());
        if (account != null) {
            AccountResponse response = accountService.updateDetails(account, accountDetailsRequest,authentication.getName());
            log.info("Account Details for username={} have been updated successfully", account.getUsername());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Change user account language.
     *
     * @param accountRequest the account language request
     * @return {@link AccountResponse} the account response entity
     */
    @PutMapping("/language")
    public ResponseEntity<AccountResponse> changeLanguage(@RequestBody AccountRequest accountRequest, Authentication authentication) {
        accountRequest.setUserName(authentication.getName());
        AccountResponse response = accountService.changeLanguage(accountRequest);
        if (response != null) {
            log.info("Account language for username={} has been changed successfully", response.getUserName());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Change user account password.
     *
     * @param accountRequest the account password request
     * @return {@link AccountResponse} the account response entity
     */
    @PutMapping("/password")
    public ResponseEntity<AccountResponse> changePassword(@RequestBody AccountRequest accountRequest, Authentication authentication) {
        accountRequest.setUserName(authentication.getName());
        AccountResponse response = userProfileService.changePassword(accountRequest);
        if (response != null) {
            if (response.isValid()) {
                log.info("Account password for username={} has been changed successfully", response.getUserName());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Validate if an account can be deactivated
     *
     * @return {@link AccountResponse} the account response
     */
    @PostMapping("/deactivation")
    public ResponseEntity<AccountResponse> validateDeactivation(Authentication authentication) {
        log.info("Validate account deactivation for username={}", authentication.getName());
        AccountResponse response = accountService.validateDeactivation(authentication.getName());
        if (response != null && response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Deactivate an account
     *
     * @return {@link AccountResponse} the account response
     */
    @PostMapping("/deactivation/confirmation")
    public ResponseEntity<AccountResponse> confirmDeactivation(Authentication authentication) {
        log.info("Deactivating account for username={}", authentication.getName());
        AccountResponse response = accountService.deactivate(authentication.getName());
        if (response != null && response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Change user account language.
     *
     * @param accountRequest the account language request
     * @return {@link AccountResponse} the account response entity
     */
    @PutMapping("/email-notification")
    public ResponseEntity<AccountResponse> modifyEmailNotification(@RequestBody AccountRequest accountRequest, Authentication authentication) {
        accountRequest.setUserName(authentication.getName());
        AccountResponse response = accountService.modifyEmailNotification(accountRequest);
        if (response != null) {
            log.info("Account email notification for username={} has been updated successfully", response.getUserName());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Lock account to be updated.
     *
     * @param lockChildRequest contains the child's username
     * @return {@link AccountResponse} the account response entity
     */
    @PostMapping("/lock")
    public ResponseEntity<AccountResponse> lockAccount(Authentication authentication, @RequestBody LockChildRequest lockChildRequest){
        log.info("Lock account for username={}", authentication.getName());
        return ResponseEntity.ok(accountService.lockAccount(authentication.getName(),lockChildRequest.getChildUserName()));
    }


}
