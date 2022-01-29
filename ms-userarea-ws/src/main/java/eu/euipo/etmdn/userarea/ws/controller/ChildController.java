/*
 * $Id:: ChildController.java 2021/03/01 09:39 dvelegra
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
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.AccountRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.ChildDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.ChildInformation;
import eu.euipo.etmdn.userarea.common.domain.ChildUpdateDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.CHILD_ACCOUNT_ACTIVATED;

/**
 * The Child controller.
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/child")
public class ChildController {

    private final ChildService childService;
    private final RegistrationService registrationService;
    private final AccountService accountService;

    /**
     * Child registration and creation account details.
     *
     * @param childDetailsRequest the child registration details request
     * @return AccountRegistrationResponse the child account registration response
     */
    @PostMapping("/registration")
    @PreAuthorize("hasRole('ROLE_ACCOUNT_ADMINISTRATOR')")
    public ResponseEntity<AccountRegistrationResponse> register(@RequestBody ChildDetailsRequest childDetailsRequest, Authentication authentication) {
        String username = authentication.getName();
        log.info("Account with username={} will register the child account with email={}", username, childDetailsRequest.getEmail());
        AccountRegistrationResponse response = registrationService.registerChild(childDetailsRequest, username);
        if (response != null && response.getMessage() == null) {
            log.info("An email to confirm the child account registration has been sent to child account with email={}", response.getEmail());
            return ResponseEntity.ok(AccountRegistrationResponse.builder()
                    .userName(response.getUserName()).message(LiteralConstants.CHILD_ACCOUNT_ADDED).build());
        } else if(response != null && response.getMessage() != null) {
            log.info("Child registration for username={} by account with username={} is not completed", response.getUserName(), username);
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get Pageable and Sortable Child details.
     *
     * @param page the page number
     * @param size the page size
     * @param sortColumn the column to be sorted
     * @param sortType the sorting type
     * @return {@link Page<ChildInformation>} the pageable child response entity
     */
    @GetMapping("/details")
    @PreAuthorize("hasRole('ROLE_ACCOUNT_ADMINISTRATOR') or hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Page<ChildInformation>> getChildAccountsDetails(@RequestParam Integer page, @RequestParam Integer size,
                                                                          @RequestParam String sortColumn, @RequestParam String sortType,
                                                                          Authentication authentication) {
        String userName = authentication.getName();
        log.info("Retrieve child details for username={}", userName);
        Page<ChildInformation> childAccounts = childService.getChildAccounts(userName, page, size, sortColumn, sortType);
        if (childAccounts != null) {
            log.info("The retrieval of child accounts for username={} is successful", userName);
            return ResponseEntity.ok(childAccounts);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Update child account details.
     *
     * @param childUpdateDetailsRequest the child details to be updated
     * @return {@link AccountResponse} the account response entity
     */
    @PutMapping("/details")
    @PreAuthorize("hasRole('ROLE_ACCOUNT_ADMINISTRATOR') and @accountServiceImpl.isAllowedToAccessChildAccount(#childUpdateDetailsRequest.userName, authentication.name)")
    public ResponseEntity<AccountResponse> update(@RequestBody ChildUpdateDetailsRequest childUpdateDetailsRequest, Authentication authentication) {
        String userName = authentication.getName();
        log.info("Account with username={} will update the child details for username={}", userName, childUpdateDetailsRequest.getUserName());
        AccountResponse response = childService.update(childUpdateDetailsRequest, userName);
        if (response != null && response.getMessage() == null) {
            log.info("Child Details for username={} have been updated successfully by account with username={}", response.getUserName(), userName);
            return ResponseEntity.ok(response);
        } else if(response != null && response.getMessage() != null) {
            log.info("Child Details for username={} have not been updated by account with username={}", response.getUserName(), userName);
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Activate child account.
     *
     * @param accountRequest the child account to be activated
     * @return {@link AccountResponse} the account response entity
     */
    @PostMapping("/activation")
    @PreAuthorize("hasRole('ROLE_ACCOUNT_ADMINISTRATOR') and @accountServiceImpl.isAllowedToAccessChildAccount(#accountRequest.userName, authentication.name)")
    public ResponseEntity<AccountResponse> activate(@RequestBody AccountRequest accountRequest, Authentication authentication) {
        String userName = authentication.getName();
        log.info("Account with username={} will activate the child account with username={}", userName, accountRequest.getUserName());
        final String activated = accountService.activate(accountRequest.getUserName());
        if (activated != null) {
            log.info("Child Details for username={} have been activated successfully by account with username={}", activated, userName);
            return ResponseEntity.ok(AccountResponse.builder().message(CHILD_ACCOUNT_ACTIVATED).userName(activated).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deactivate child account.
     *
     * @param accountRequest the account account to be deactivated
     * @return {@link AccountResponse} the account response entity
     */
    @PostMapping("/deactivation")
    @PreAuthorize("hasRole('ROLE_ACCOUNT_ADMINISTRATOR') and @accountServiceImpl.isAllowedToAccessChildAccount(#accountRequest.userName, authentication.name)")
    public ResponseEntity<AccountResponse> deactivate(@RequestBody AccountRequest accountRequest, Authentication authentication) {
        String userName = authentication.getName();
        log.info("Account with username={} will deactivate the child account for username={}", userName, accountRequest.getUserName());
        AccountResponse response = accountService.deactivate(accountRequest.getUserName());
        if (response != null) {
            log.info("Child Details for username={} have been deactivated successfully by account with username={}", response.getUserName(), userName);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

}
