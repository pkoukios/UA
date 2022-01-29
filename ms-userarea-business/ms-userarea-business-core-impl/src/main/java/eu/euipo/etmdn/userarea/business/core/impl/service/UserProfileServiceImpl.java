/*
 * $Id:: UserProfileServiceImpl.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.UserProfileService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.domain.AccountRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Account service.
 */
@Slf4j
@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Instantiates Account service.
     *
     * @param userService the user service
     * @param passwordEncoder the password encoder
     */
    @Autowired
    public UserProfileServiceImpl(final UserService userService, final PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Change the current password for the specific username.
     *
     * @param accountRequest the new account request to change password
     * @return {@link AccountResponse} the account response
     */
    @Override
    public AccountResponse changePassword(AccountRequest accountRequest) {
        DomainUser user = userService.getUserByUsername(accountRequest.getUserName());
        if (user != null) {
            //Check if the password provided is the same with the password stored in the database
            if(!passwordEncoder.matches(accountRequest.getOldPassword(), user.getPassword())) {
                return AccountResponse.builder().userName(accountRequest.getUserName())
                        .valid(false)
                        .message(LiteralConstants.PASSWORD_NOT_MATCH)
                        .build();
            }
            //Change the password
            userService.setOrUpdatePassword(user.getDomainAccount().getEmail(), accountRequest.getNewPassword());
            return AccountResponse.builder().userName(accountRequest.getUserName())
                    .valid(true)
                    .message(LiteralConstants.PASSWORD_CHANGE_SUCCESS)
                    .build();
        }
        return null;
    }
}
