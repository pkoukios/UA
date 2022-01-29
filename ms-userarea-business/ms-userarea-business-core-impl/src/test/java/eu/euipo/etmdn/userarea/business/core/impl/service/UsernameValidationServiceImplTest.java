/*
 * $Id:: UsernameValidationServiceImplTest.java 2021/03/01 09:07 dvelegra
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
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.exception.IllegalUsernameException;
import eu.euipo.etmdn.userarea.common.domain.exception.ReusedUsernameException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsernameValidationServiceImplTest {

    @InjectMocks
    private UsernameValidationServiceImpl usernameValidationService;
    @Mock
    private UserService userService;
    @Mock
    private UsernameValidationRulesConfig usernameValidationRulesConfig;

    private DomainUser user;
    private AccountRegistrationRequest accountRegistrationRequest;
    private static final String username = "abc@xyz.com";
    private static final String email = "abc@xyz.com";
    private static final String pattern = "^[a-z0-9]{6,100}$";

    @Before
    public void setUp() {
        usernameValidationService = new UsernameValidationServiceImpl(userService, usernameValidationRulesConfig);
        when(usernameValidationRulesConfig.getPattern()).thenReturn(pattern);
        user = DomainUser.builder().username(username).build();
        accountRegistrationRequest = AccountRegistrationRequest.builder()
                .build();
    }

    @Test
    public void testValidateValidUsername() {
        String username = "aasf123";
        accountRegistrationRequest.setUserName(username);
        user.setUsername(username);
        when(userService.getUserByUsername(username)).thenReturn(null);
        usernameValidationService.validate(username, email);
    }

    @Test(expected = IllegalUsernameException.class)
    public void testValidateNotValidUsernameWithLessThan6Char() {
        String username = "aas12";
        user.setUsername(username);
        usernameValidationService.validate(username, null);
    }

    @Test(expected = IllegalUsernameException.class)
    public void testValidateNotValidUsernameWithCapital() {
        String username = "abcF1as";
        user.setUsername(username);
        usernameValidationService.validate(username, null);
    }

    @Test(expected = ReusedUsernameException.class)
    public void testValidateNotValidUsernameAlreadyUsed() {
        String username = "abc123as";
        accountRegistrationRequest.setUserName(username);
        user.setUsername(username);
        when(userService.getUserByUsername(username)).thenReturn(user);
        usernameValidationService.validate(username, email);
    }

}