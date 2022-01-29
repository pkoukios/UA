/*
 * $Id:: ChildServiceImpl.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service;

import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.business.service.AuthorityService;
import eu.euipo.etmdn.userarea.common.business.service.ChildService;
import eu.euipo.etmdn.userarea.common.business.service.RegistrationService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.business.utils.AccountUtils;
import eu.euipo.etmdn.userarea.common.business.utils.LockUtils;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.AccountType;
import eu.euipo.etmdn.userarea.common.domain.ChildInformation;
import eu.euipo.etmdn.userarea.common.domain.ChildUpdateDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.DomainAuthority;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.persistence.entity.ChildAccount;
import eu.euipo.etmdn.userarea.common.persistence.repository.ChildAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The Child service.
 */
@Slf4j
@Service
@Transactional
public class ChildServiceImpl implements ChildService {

    private final ChildAccountRepository childAccountRepository;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final AuthorityService authorityService;
    private final AccountService accountService;

    /**
     * Instantiates Child service.
     * @param childAccountRepository the child repository
     * @param userService the user service
     * @param registrationService the registration service
     * @param authorityService the authority service
     * @param accountService
     */
    @Autowired
    public ChildServiceImpl(final ChildAccountRepository childAccountRepository, final UserService userService,
                            final RegistrationService registrationService, final AuthorityService authorityService, AccountService accountService) {
        this.childAccountRepository = childAccountRepository;
        this.userService = userService;
        this.registrationService = registrationService;
        this.authorityService = authorityService;
        this.accountService = accountService;
    }

    /**
     * Retrieve the children accounts of the specified username.
     *
     * @param userName the account username
     * @param page the page number
     * @param size the page size
     * @param sortColumn the column to be sorted
     * @param sortType the sorting type
     * @return {@link Page<ChildAccount>} the user accounts
     */
    @Override
    public Page<ChildInformation> getChildAccounts(String userName, Integer page, Integer size, String sortColumn, String sortType) {
        List<DomainAuthority> domainAuthorities = authorityService.getAllAuthorities();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortColumn));
        DomainUser user = userService.getUserByUsername(userName);
        if (user != null && AccountType.CHILD.name().equals(user.getDomainAccount().getType())) {
            userName = childAccountRepository.findByUsername(userName).getMainAccountName();
        }
        if (userName != null) {
            Page<ChildAccount> childAccounts = childAccountRepository.findByMainAccountName(userName, pageable);
            List<ChildInformation> childInformationList = AccountUtils.convertTotoChildInformation(childAccounts.getContent(), domainAuthorities);
            return new PageImpl<>(childInformationList, childAccounts.getPageable(), childAccounts.getTotalElements());
        }
        return null;
    }

    /**
     * Update child account for the specific username.
     *
     * @param childUpdateDetailsRequest the child details
     * @return {@link AccountResponse} the account response
     */
    @Override
    public AccountResponse update(ChildUpdateDetailsRequest childUpdateDetailsRequest, String username) {
        List<DomainAuthority> domainAuthorities = authorityService.getAllAuthorities();
        if(!userService.checkRolesWithDependencies(childUpdateDetailsRequest.getRoles(), domainAuthorities)) {
            return AccountResponse.builder().userName(childUpdateDetailsRequest.getUserName()).message(LiteralConstants.ENABLE_IP_RIGHT_ACCESS).build();
        }
        ChildAccount existingChildAccount = childAccountRepository.findByUsername(childUpdateDetailsRequest.getUserName());
        if(existingChildAccount == null) {
            return null;
        }
        DomainUser user = userService.getUserByUsername(existingChildAccount.getUsername());
        if (childUpdateDetailsRequest.getFirstName() != null) {
            existingChildAccount.setFirstName(childUpdateDetailsRequest.getFirstName());
        }
        if (childUpdateDetailsRequest.getLastName() != null) {
            existingChildAccount.setSurName(childUpdateDetailsRequest.getLastName());
        }
        if (childUpdateDetailsRequest.getRoles() != null) {
            user.setAuthorities(AccountUtils.buildDomainAccountAuthoritiesFromRoles(childUpdateDetailsRequest.getRoles(), domainAuthorities));
            userService.save(user);
            existingChildAccount.setRoles(StringUtils.join(childUpdateDetailsRequest.getRoles(), ","));
        }
        this.releaseLockAndSaveAccount(existingChildAccount,username);
        return AccountResponse.builder().userName(childUpdateDetailsRequest.getUserName()).isLocked(false).build();
    }

    private void releaseLockAndSaveAccount(ChildAccount existingChildAccount,String userName){
        LockUtils.checkAccountLockedAndThrowException(existingChildAccount.getLockedBy(), userName);
        existingChildAccount.setLockedBy(null);
        existingChildAccount.setLockedDate(null);
        childAccountRepository.save(existingChildAccount);
    }

}
