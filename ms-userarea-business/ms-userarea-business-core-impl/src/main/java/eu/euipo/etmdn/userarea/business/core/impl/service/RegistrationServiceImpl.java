/*
 * $Id:: RegistrationServiceImpl.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.event.AuditEventPublisher;
import eu.euipo.etmdn.userarea.common.business.service.AccountEmailNotificationService;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.business.service.PasswordValidationService;
import eu.euipo.etmdn.userarea.common.business.service.RegistrationService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.business.service.UserTokenService;
import eu.euipo.etmdn.userarea.common.business.service.UsernameValidationService;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.ChildDetailsRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.DomainUserToken;
import eu.euipo.etmdn.userarea.common.domain.auditlog.AuditType;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.email.EmailUser;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.REGISTRATION_TOKEN_NOT_VALID;

/**
 * The Registration service.
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private UsernameValidationService usernameValidationService;
    @Autowired
    private PasswordValidationService passwordValidationService;
    @Autowired
    private AccountEmailNotificationService accountEmailNotificationService;
    @Autowired
    private AuditEventPublisher auditEventPublisher;

    /**
     * Validate the account registration request information.
     *
     * @param userName the userName
     * @param email the email
     * @param password the password
     */
    @Override
    public void validate(String userName, String email, String password) {
        // validate username
        usernameValidationService.validate(userName, email);
        // validate password
        passwordValidationService.validateForRegistration(password);
    }

    /**
     * Register person account.
     *
     * @param accountRegistrationRequest the person account registration request
     * @return {@link EmailUser} the email user details
     */
    @Override
    public EmailUser register(AccountRegistrationRequest accountRegistrationRequest, DomainAccount account) {
        DomainUser registeredUser = userService.registerUser(accountRegistrationRequest.getUserName(),
                    accountRegistrationRequest.getPassword(), account);
        return accountEmailNotificationService.sendRegistrationEmail(registeredUser.getDomainAccount());
    }

    /**
     * Register child account.
     *
     * @param childDetailsRequest the child account registration details
     * @return {@link EmailUser} the email user details
     */
    @Override
    public AccountRegistrationResponse registerChild(ChildDetailsRequest childDetailsRequest, String userName) {
        this.validate(childDetailsRequest.getEmail(), childDetailsRequest.getEmail(), null);
        DomainUser registeredChild = userService.registerChild(childDetailsRequest, userName);
        if( registeredChild == null) {
            auditEventPublisher.publishCustomEvent("Account verification attempt not successful", userName, AuditType.ACCOUNT.getValue());
            return AccountRegistrationResponse.builder().userName(childDetailsRequest.getEmail())
                    .message(LiteralConstants.ENABLE_IP_RIGHT_ACCESS).build();
        }
        accountEmailNotificationService.sendRegistrationEmail(registeredChild.getDomainAccount());
        return AccountRegistrationResponse.builder().userName(childDetailsRequest.getEmail()).build();
    }

    /**
     * Confirm user account registration.
     *
     * @param confirmRegistrationRequest the account registration confirmation request
     * @return {@link ConfirmRegistrationResponse} the confirm registration response
     */
    @Override
    public ConfirmRegistrationResponse confirm(ConfirmRegistrationRequest confirmRegistrationRequest) {
        DomainUserToken userToken = userTokenService.validateTokenExpiration(confirmRegistrationRequest.getToken());
        if (userToken != null) {
            if(confirmRegistrationRequest.getPassword() != null) {
                DomainUser user = userService.getUserByUsername(userToken.getUsername());
                userService.setOrUpdatePassword(user.getDomainAccount().getEmail(), confirmRegistrationRequest.getPassword());
            }
            DomainAccount account = accountService.verifyAccountRegistration(userToken.getUsername());
            userTokenService.inValidateToken(userToken);
            log.info("Successful account registration for for user={}", account.getUsername());
            auditEventPublisher.publishCustomEvent("Account verification attempt successful", account.getUsername(), AuditType.ACCOUNT.getValue());
            return ConfirmRegistrationResponse.builder().userName(account.getUsername()).valid(Boolean.TRUE).build();
        }
        return ConfirmRegistrationResponse.builder().valid(Boolean.FALSE).message(REGISTRATION_TOKEN_NOT_VALID).build();
    }

    /**
     * Registration resend token.
     *
     * @param confirmRegistrationRequest the account registration confirmation request
     * @return {@link ConfirmRegistrationResponse} the confirm registration response
     */
    @Override
    public ConfirmRegistrationResponse resendToken(ConfirmRegistrationRequest confirmRegistrationRequest) {
        if (confirmRegistrationRequest.getToken() != null) {
            DomainUserToken userToken = userTokenService.getUserTokenByToken(confirmRegistrationRequest.getToken());
            if (userToken != null) {
                DomainUser user = userService.getUserByUsername(userToken.getUsername());
                return resendEmailWithToken(user);
            }
        } else if (confirmRegistrationRequest.getUserName() != null) {
            DomainUser user = userService.getUserByUsername(confirmRegistrationRequest.getUserName());
            return resendEmailWithToken(user);
        }
        return null;
    }

    private ConfirmRegistrationResponse resendEmailWithToken(DomainUser user) {
        EmailUser emailUser = accountEmailNotificationService.sendRegistrationEmail(user.getDomainAccount());
        log.info("Send email for account registration for user={}", emailUser.getUserName());
        return ConfirmRegistrationResponse.builder().userName(user.getUsername()).valid(Boolean.TRUE).build();
    }
}
