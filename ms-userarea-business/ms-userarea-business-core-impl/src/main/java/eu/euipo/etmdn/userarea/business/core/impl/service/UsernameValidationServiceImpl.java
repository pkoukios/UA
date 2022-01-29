/*
 * $Id:: UsernameValidationServiceImpl.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.config.UsernameValidationRulesConfig;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.business.service.UsernameValidationService;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.exception.IllegalUsernameException;
import eu.euipo.etmdn.userarea.common.domain.exception.ReusedUsernameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * UsernameValidationServiceImpl class
 */
@Service
public class UsernameValidationServiceImpl implements UsernameValidationService {

    private final UserService userService;
    private final UsernameValidationRulesConfig usernameValidationRulesConfig;

    /**
     * Instantiates UsernameValidation service.
     *
     * @param userService the user service
     * @param usernameValidationRulesConfig the username validation rules config
     */
    @Autowired
    public UsernameValidationServiceImpl(UserService userService, UsernameValidationRulesConfig usernameValidationRulesConfig) {
        this.userService = userService;
        this.usernameValidationRulesConfig = usernameValidationRulesConfig;
    }

    /**
     * Validates a username against configured rules and user existing entries in DB.
     *
     * @param userName the userName
     * @param email the email
     */
    @Override
    public void validate(String userName, String email) {
        if (email == null) {
            boolean result = Pattern.matches(usernameValidationRulesConfig.getPattern(), userName);
            if (!result) {
                throw new IllegalUsernameException(userName);
            }
        }
        validateUsernameDBExistence(userName);
    }

    /**
     * Check if a username exists in DB otherwise throws ReusedUsernameException.
     *
     * @param username the username
     */
    private void validateUsernameDBExistence(String username) {
        DomainUser user = userService.getUserByUsername(username);
        if (user != null) {
            throw new ReusedUsernameException(username);
        }
    }

}