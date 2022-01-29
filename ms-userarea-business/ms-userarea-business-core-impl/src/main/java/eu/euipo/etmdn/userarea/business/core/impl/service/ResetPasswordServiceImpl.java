/*
 * $Id:: ResetPasswordServiceImpl.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.service.EmailService;
import eu.euipo.etmdn.userarea.common.business.service.ResetPasswordService;
import eu.euipo.etmdn.userarea.common.business.service.UserService;
import eu.euipo.etmdn.userarea.common.business.service.UserTokenService;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetResponse;
import eu.euipo.etmdn.userarea.common.domain.DomainUser;
import eu.euipo.etmdn.userarea.common.domain.DomainUserToken;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenRequest;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenResponse;
import eu.euipo.etmdn.userarea.common.domain.email.EmailUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants.PASSWORD_RESET_LINK_NOT_VALID;

@Slf4j
@Service
@Transactional
/*
 * Reset Password Service implementation
 */
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final UserTokenService userTokenService;
    private final UserService userService;
    private final EmailService emailService;

    /**
     * Instantiates a new Reset Password service.
     *
     * @param userTokenService the user token service
     * @param userService the user service
     * @param emailService the email service
     */
    @Autowired
    public ResetPasswordServiceImpl(final UserTokenService userTokenService, final UserService userService,
                                    final EmailService emailService) {
        this.userTokenService = userTokenService;
        this.userService = userService;
        this.emailService = emailService;
    }

    /**
     * Validate reset token
     *
     * @param validateTokenRequest the reset password request
     * @return ValidateTokenResponse the validate token response
     */
    @Override
    public ValidateTokenResponse validate(ValidateTokenRequest validateTokenRequest) {
        DomainUserToken userToken = userTokenService.validateTokenExpiration(validateTokenRequest.getToken());
        if (userToken != null) {
            log.info("Token is valid for user={}", userToken.getUsername());
            DomainUser user = userService.getUserByUsername(userToken.getUsername());
            if (user != null) {
                return ValidateTokenResponse.builder()
                        .email(user.getDomainAccount().getEmail())
                        .valid(Boolean.TRUE)
                        .build();
            }
        }
        log.info("Not valid token={}", validateTokenRequest.getToken());
        return ValidateTokenResponse.builder()
                .valid(Boolean.FALSE).message(PASSWORD_RESET_LINK_NOT_VALID).build();
    }

    /**
     * Confirm password reset
     *
     * @param confirmPasswordResetRequest the confirm password request
     * @return ConfirmPasswordResetResponse the confirm password reset response
     */
    @Override
    public ConfirmPasswordResetResponse confirm(ConfirmPasswordResetRequest confirmPasswordResetRequest) {
        DomainUserToken userToken = userTokenService.validateTokenExpiration(confirmPasswordResetRequest.getToken());
        if (userToken != null) {
            DomainUser user = userService.getUserByUsername(userToken.getUsername());
            if (user != null) {
                userService.setOrUpdatePassword(user.getDomainAccount().getEmail(), confirmPasswordResetRequest.getPassword());
                userTokenService.inValidateToken(userToken);
                EmailUser emailUser = userService.getEmailUserDetails(user.getDomainAccount(), null);
                emailService.sendConfirmPasswordMessageEmail(emailUser);
                log.info("Email has been sent to email={} for successful password change", emailUser.getEmail());
                return ConfirmPasswordResetResponse.builder()
                        .email(emailUser.getEmail()).valid(Boolean.TRUE).build();
            }
            log.info("User not found with username={}", userToken.getUsername());
            return null;
        }
        log.info("Not valid token");
        return ConfirmPasswordResetResponse.builder()
                .valid(Boolean.FALSE).message(PASSWORD_RESET_LINK_NOT_VALID).build();
    }
}
