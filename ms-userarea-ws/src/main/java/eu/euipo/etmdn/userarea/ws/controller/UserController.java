/*
 * $Id:: UserController.java 2021/03/01 09:07 dvelegra
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

import eu.euipo.etmdn.userarea.common.business.mapper.AccountMapper;
import eu.euipo.etmdn.userarea.common.business.service.AccountEmailNotificationService;
import eu.euipo.etmdn.userarea.common.business.service.RegistrationService;
import eu.euipo.etmdn.userarea.common.business.service.ResetPasswordService;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.AccountRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmPasswordResetResponse;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationRequest;
import eu.euipo.etmdn.userarea.common.domain.ConfirmRegistrationResponse;
import eu.euipo.etmdn.userarea.common.domain.ResetPasswordRequest;
import eu.euipo.etmdn.userarea.common.domain.ResetPasswordResponse;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenRequest;
import eu.euipo.etmdn.userarea.common.domain.ValidateTokenResponse;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.email.EmailUser;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The User controller.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ResetPasswordService resetPasswordService;
    @Autowired
    private AccountEmailNotificationService accountEmailNotificationService;


    /**
     * Reset password method.
     *
     * @param resetPasswordRequest the reset password request
     * @return ResetPasswordResponse the resetPasswordResponse
     */
    @PostMapping("/password/reset")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("Reset password request for email={}", resetPasswordRequest.getEmail());
        ResetPasswordResponse resetPasswordResponse = accountEmailNotificationService.sendResetPasswordEmail(resetPasswordRequest.getEmail());
        return ResponseEntity.ok(resetPasswordResponse);
    }

    /**
     * Validate reset token method.
     *
     * @param validateTokenRequest the reset password request
     * @return ValidateTokenResponse the validateTokenResponse
     */
    @PostMapping("/password/validate-token")
    public ResponseEntity<ValidateTokenResponse> validateToken(@RequestBody ValidateTokenRequest validateTokenRequest) {
        ValidateTokenResponse validateTokenResponse = resetPasswordService.validate(validateTokenRequest);
        return ResponseEntity.ok(validateTokenResponse);
    }

    /**
     * Confirm password method.
     *
     * @param confirmPasswordResetRequest the confirm password request
     * @return ConfirmPasswordResetResponse the confirm password reset response
     */
    @PostMapping("/password/confirmation")
    public ResponseEntity<ConfirmPasswordResetResponse> confirmPasswordReset(@RequestBody ConfirmPasswordResetRequest confirmPasswordResetRequest) {
        ConfirmPasswordResetResponse confirmPasswordResetResponse = resetPasswordService.confirm(confirmPasswordResetRequest);
        if (confirmPasswordResetResponse != null) return ResponseEntity.ok(confirmPasswordResetResponse);
        return ResponseEntity.notFound().build();
    }

    /**
     * User account registration and validation details.
     *
     * @param accountRegistrationRequest the account registration request
     * @return RegistrationResponse the registration response
     */
    @PostMapping("/registration/validation")
    public ResponseEntity<AccountRegistrationResponse> registerForValidation(@RequestBody AccountRegistrationRequest accountRegistrationRequest) {
        log.info("Register and validate user account with username={}", accountRegistrationRequest.getUserName());
        registrationService.validate(accountRegistrationRequest.getUserName(), accountRegistrationRequest.getEmail(),
                accountRegistrationRequest.getPassword());
        log.info("Success Validation of user's login details with username={}", accountRegistrationRequest.getUserName());
        return ResponseEntity.ok(AccountRegistrationResponse.builder().userName(accountRegistrationRequest.getUserName()).build());
    }

    /**
     * User registration and creation account details.
     *
     * @param accountRegistrationRequest the account registration request
     * @return AccountRegistrationResponse the account registration response
     */
    @PostMapping("/registration/creation")
    public ResponseEntity<AccountRegistrationResponse> userAccountRegistration(@RequestBody AccountRegistrationRequest accountRegistrationRequest) {
        log.info("Registration for user account with username={}", accountRegistrationRequest.getUserName());
        EmailUser user = registrationService.register(accountRegistrationRequest, AccountMapper.MAPPER.fromRegistration(accountRegistrationRequest));
        log.info("An email to confirm the user account registration has been sent to user with email={}", user.getEmail());
        return ResponseEntity.ok(AccountRegistrationResponse.builder()
                .userName(user.getUserName()).message(LiteralConstants.REGISTRATION_MESSAGE).build());
    }

    /**
     * Confirm user registration.
     *
     * @param confirmRegistrationRequest the confirm registration request
     * @return ConfirmRegistrationResponse the confirm registration response
     */
    @PostMapping("/registration/confirmation")
    public ResponseEntity<ConfirmRegistrationResponse> confirmRegistration(@RequestBody ConfirmRegistrationRequest confirmRegistrationRequest) {
        log.info("Account registration confirmation");
        ConfirmRegistrationResponse confirmRegistrationResponse = registrationService.confirm(confirmRegistrationRequest);
        if (confirmRegistrationResponse.isValid()) {
           return ResponseEntity.ok(confirmRegistrationResponse);
        }
        log.info("Not valid token for user account registration");
        return ResponseEntity.badRequest().body(confirmRegistrationResponse);
    }

    /**
     * User request new registration email for account verification.
     *
     * @param confirmRegistrationRequest the resend registration request
     * @return ConfirmRegistrationResponse the resend registration response
     */
    @PostMapping("/registration/resend-token")
    public ResponseEntity<ConfirmRegistrationResponse> resendTokenForRegistration(@RequestBody ConfirmRegistrationRequest confirmRegistrationRequest) {
        log.info("Resend token for account registration");
        ConfirmRegistrationResponse confirmRegistrationResponse = registrationService.resendToken(confirmRegistrationRequest);
        if (confirmRegistrationResponse != null) {
            return ResponseEntity.ok(confirmRegistrationResponse);
        }
        log.info("UserArea could not resend token for user registration");
        return ResponseEntity.notFound().build();
    }

}
