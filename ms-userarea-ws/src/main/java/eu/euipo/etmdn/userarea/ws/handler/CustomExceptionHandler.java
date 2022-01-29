/*
 * $Id:: CustomExceptionHandler.java 2021/03/01 09:07 dvelegra
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

package eu.euipo.etmdn.userarea.ws.handler;


import eu.euipo.etmdn.userarea.common.domain.ErrorResponse;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.exception.AccountEntityLockedException;
import eu.euipo.etmdn.userarea.common.domain.exception.AccountNotVerifiedException;
import eu.euipo.etmdn.userarea.common.domain.exception.ApplicationEntityLockedException;
import eu.euipo.etmdn.userarea.common.domain.exception.AttachmentFileNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.DraftAccountNotAllowedException;
import eu.euipo.etmdn.userarea.common.domain.exception.DraftMaxRepliesReachedException;
import eu.euipo.etmdn.userarea.common.domain.exception.DraftStatusSentException;
import eu.euipo.etmdn.userarea.common.domain.exception.DueDateReplyException;
import eu.euipo.etmdn.userarea.common.domain.exception.EmailSendException;
import eu.euipo.etmdn.userarea.common.domain.exception.FrontofficeReceiptNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.FrontofficeServerException;
import eu.euipo.etmdn.userarea.common.domain.exception.IllegalPasswordException;
import eu.euipo.etmdn.userarea.common.domain.exception.IllegalUsernameException;
import eu.euipo.etmdn.userarea.common.domain.exception.InvalidAttachmentException;
import eu.euipo.etmdn.userarea.common.domain.exception.InvalidDownloadAttachmentUserException;
import eu.euipo.etmdn.userarea.common.domain.exception.InvalidNumberAttachmentException;
import eu.euipo.etmdn.userarea.common.domain.exception.MessageEntityLockedException;
import eu.euipo.etmdn.userarea.common.domain.exception.NotVerifiedAccountToReceiveEmailException;
import eu.euipo.etmdn.userarea.common.domain.exception.NoteEntityLockedException;
import eu.euipo.etmdn.userarea.common.domain.exception.PaymentClientException;
import eu.euipo.etmdn.userarea.common.domain.exception.PaymentCompletedAnotherUserException;
import eu.euipo.etmdn.userarea.common.domain.exception.ReusedPasswordException;
import eu.euipo.etmdn.userarea.common.domain.exception.ReusedUsernameException;
import eu.euipo.etmdn.userarea.common.domain.exception.SSOLoginException;
import eu.euipo.etmdn.userarea.common.domain.exception.ServiceUnavailableException;
import eu.euipo.etmdn.userarea.common.domain.exception.SignatureClientException;
import eu.euipo.etmdn.userarea.common.domain.exception.UnsupportedContentTypeException;
import eu.euipo.etmdn.userarea.common.domain.exception.ValidateEserviceException;
import eu.euipo.etmdn.userarea.domain.shoppingcart.exception.ShoppingCartSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.persistence.LockTimeoutException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import static eu.euipo.etmdn.userarea.domain.constants.UserareaConstants.MAX_UPLOAD_SIZE;

/**
 * The Custom exception handler.
 */
@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String USERNAME = "username";
    private static final String EXCEPTION = "exception";
    private static final String MESSAGE = "message";

    /**
     * Handle UsernameNotFoundException.
     *
     * @param e the UsernameNotFoundException
     * @return the response entity
     */
    @ExceptionHandler({UsernameNotFoundException.class})
    public final ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        logger.info("Username not found");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, UsernameNotFoundException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle SSOLoginException.
     *
     * @param e the SSOLoginException
     * @return the response entity
     */
    @ExceptionHandler({SSOLoginException.class})
    public final ResponseEntity<ErrorResponse> handleSSOLoginException(SSOLoginException e) {
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, SSOLoginException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }


    /**
     * Handle EmailSendException.
     *
     * @param e the EmailSendException
     * @return the response entity
     */
    @ExceptionHandler({EmailSendException.class})
    public final ResponseEntity<ErrorResponse> handleSendEmailException(EmailSendException e) {
        logger.info("Email could not be sent due to exception");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, EmailSendException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.EMAIL_SEND_ERROR, HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle BadCredentialsException.
     *
     * @param e the BadCredentialsException
     * @return the response entity
     */
    @ExceptionHandler({BadCredentialsException.class})
    public final ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        logger.info("Credentials are not valid");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, BadCredentialsException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle LockedException.
     *
     * @param e the LockedException
     * @return the response entity
     */
    @ExceptionHandler(LockedException.class)
    public final ResponseEntity<ErrorResponse> handleLockedException(LockedException e) {
        logger.info("Account id locked");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, LockedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ACCOUNT_LOCKED, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle DisabledException.
     *
     * @param e the DisabledException
     * @return the response entity
     */
    @ExceptionHandler(DisabledException.class)
    public final ResponseEntity<ErrorResponse> handleDisabledException(DisabledException e) {
        logger.info("Account is deactivated(disabled)");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, DisabledException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle AccountExpiredException.
     *
     * @param e the AccountExpiredException
     * @return the response entity
     */
    @ExceptionHandler(AccountExpiredException.class)
    public final ResponseEntity<ErrorResponse> handleAccountExpiredException(AccountExpiredException e) {
        logger.info("Account is expired");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, AccountExpiredException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ACCOUNT_EXPIRED, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle AccountNotVerifiedException.
     *
     * @param e the AccountNotVerifiedException
     * @return the response entity
     */
    @ExceptionHandler(AccountNotVerifiedException.class)
    public final ResponseEntity<ErrorResponse> handleAccountNotVerifiedException(AccountNotVerifiedException e) {
        logger.info("Account is not verified");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, AccountNotVerifiedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ACCOUNT_NOT_VERIFIED, HttpStatus.UNAUTHORIZED.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle IllegalPasswordException.
     *
     * @param e the IllegalPasswordException
     * @return the response entity
     */
    @ExceptionHandler({IllegalPasswordException.class})
    public final ResponseEntity<ErrorResponse> handleIllegalPasswordException(IllegalPasswordException e) {
        Map<String, String> details = new HashMap<>();
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ILLEGAL_PASSWORD, HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ReusedPasswordException.
     *
     * @param e the ReusedPasswordException
     * @return the response entity
     */
    @ExceptionHandler({ReusedPasswordException.class})
    public final ResponseEntity<ErrorResponse> handleReusedPasswordException(ReusedPasswordException e) {
        Map<String, String> details = new HashMap<>();
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.REUSED_PASSWORD, HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalUsernameException.
     *
     * @param e the IllegalUsernameException
     * @return the response entity
     */
    @ExceptionHandler({IllegalUsernameException.class})
    public final ResponseEntity<ErrorResponse> handleIllegalUsernameException(IllegalPasswordException e) {
        Map<String, String> details = new HashMap<>();
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ILLEGAL_USERNAME, HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ReusedUsernameException.
     *
     * @param e the ReusedUsernameException
     * @return the response entity
     */
    @ExceptionHandler({ReusedUsernameException.class})
    public final ResponseEntity<ErrorResponse> handleReusedUsernameException(ReusedUsernameException e) {
        Map<String, String> details = new HashMap<>();
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.REUSED_USERNAME, HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handle ValidateEserviceException.
     *
     * @param e the ValidateEserviceException
     * @return the response entity
     */
    @ExceptionHandler({ValidateEserviceException.class})
    public final ResponseEntity<ErrorResponse> handleValidateEserviceException(ValidateEserviceException e) {
        logger.info("Validation for Eservice failed");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, ValidateEserviceException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle EntityNotFoundException.
     *
     * @param e the EntityNotFoundException
     * @return the response entity
     */
    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, EntityNotFoundException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(e.getLocalizedMessage(), HttpStatus.NOT_FOUND.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle ServiceUnavailableException.
     *
     * @param e the ServiceUnavailableException
     * @return the response entity
     */
    @ExceptionHandler({ServiceUnavailableException.class})
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException e) {
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, ServiceUnavailableException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(e.getLocalizedMessage(), HttpStatus.SERVICE_UNAVAILABLE.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * LockTimeoutException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler(LockTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleLockTimeout(LockTimeoutException e) {
        log.info("Handling lock timeout exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, LockTimeoutException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(e.getLocalizedMessage(), HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * MessageEntityLockedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({MessageEntityLockedException.class})
    public ResponseEntity<ErrorResponse> handleMessageEntityLockedException(MessageEntityLockedException e) {
        log.info("Handling entity locked exception");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, MessageEntityLockedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.CORRESPONDENCE_MESSAGE_ENTITY_LOCKED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * ApplicationEntityLockedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({ApplicationEntityLockedException.class})
    public ResponseEntity<ErrorResponse> handleApplicationEntityLockedException(ApplicationEntityLockedException e) {
        log.info("Handling entity locked exception");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, ApplicationEntityLockedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.APPLICATION_ENTITY_LOCKED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * DraftMaxRepliesReachedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({DraftMaxRepliesReachedException.class})
    public ResponseEntity<ErrorResponse> handleDraftMaxRepliesReachedException(DraftMaxRepliesReachedException e) {
        log.info("Handling draft max replies exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, DraftMaxRepliesReachedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.DRAFT_MAX_REPLIES_REACHED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * InvalidAttachmentException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({InvalidAttachmentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidAttachmentException(InvalidAttachmentException e) {
        log.info("Handling invalid attachment exception");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, e.getLocalizedMessage());
        details.put(EXCEPTION, InvalidAttachmentException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.INVALID_ATTACHMENT, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * AttachmentFileNotFoundException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({AttachmentFileNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAttachmentFileNotFoundException(AttachmentFileNotFoundException e) {
        log.info("Handling attachment file not found exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, AttachmentFileNotFoundException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ATTACHMENT_NOT_FOUND, HttpStatus.NOT_FOUND.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * InvalidNumberAttachmentException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({InvalidNumberAttachmentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidNumberAttachmentException(InvalidNumberAttachmentException e) {
        log.info("Handling invalid number of attachments exception");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, e.getLocalizedMessage());
        details.put(EXCEPTION, InvalidNumberAttachmentException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.DRAFT_ATTACHMENT_MAX_NUMBER, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * MaxUploadSizeExceededException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResponse> maxFileUploadSizeError(MaxUploadSizeExceededException ex) {
        log.info("Handling invalid file size error");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, MAX_UPLOAD_SIZE);
        details.put(EXCEPTION, MaxUploadSizeExceededException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.MAX_FILE_SIZE, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * ShoppingCartSecurityException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({ShoppingCartSecurityException.class})
    public ResponseEntity<ErrorResponse> handleShoppingCartSecurityException(ShoppingCartSecurityException e) {
        log.info("Handling shopping cart security exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, ShoppingCartSecurityException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(e.getLocalizedMessage(), HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * SignatureClientException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({SignatureClientException.class})
    public ResponseEntity<ErrorResponse> handleSignatureClientException(SignatureClientException ex) {
        log.info("Handling External Signature Platform exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, SignatureClientException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse("External Signature Platform error", HttpStatus.INTERNAL_SERVER_ERROR.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * SocketTimeoutException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({SocketTimeoutException.class})
    public ResponseEntity<ErrorResponse> handleSocketTimeoutException(SocketTimeoutException ex) {
        log.info("Handling Socket Timeout Exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, SocketTimeoutException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DueDateReplyException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({DueDateReplyException.class})
    public ResponseEntity<ErrorResponse> handleDueDateError(DueDateReplyException ex) {
        log.info("Handling error for replying to Draft with expired due date");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, ex.getLocalizedMessage());
        details.put(EXCEPTION, DueDateReplyException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.EXPIRED_DRAFT_REPLY, HttpStatus.BAD_REQUEST.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * UnsupportedContentTypeException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({UnsupportedContentTypeException.class})
    public ResponseEntity<ErrorResponse> handleUnsupportedContentTypeError(UnsupportedContentTypeException ex) {
        log.info("Handling error for unsupported attachment content type");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, ex.getLocalizedMessage());
        details.put(EXCEPTION, UnsupportedContentTypeException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.UNSUPPORTED_FILE_FORMAT, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({InvalidDownloadAttachmentUserException.class})
    public ResponseEntity<ErrorResponse> handleOtherUserDraftAttachmentError(
            InvalidDownloadAttachmentUserException ex) {
        log.info("Handling error for invalid user downloading draft attachment");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, ex.getLocalizedMessage());
        details.put(EXCEPTION, InvalidDownloadAttachmentUserException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.DOWNLOAD_ATTACHMENT_NOT_ALLOWED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * DraftMaxRepliesReachedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({DraftStatusSentException.class})
    public ResponseEntity<ErrorResponse> handleDraftStatusSentException(DraftStatusSentException e) {
        log.info("Handling draft status sent exception");
        Map<String, String> details = new HashMap<>();
        details.put(EXCEPTION, DraftStatusSentException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.DRAFT_SENT_STATUS_NO_ACTION_ALLOWED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * DraftMaxRepliesReachedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({DraftAccountNotAllowedException.class})
    public ResponseEntity<ErrorResponse> handleDraftAccountNotAllowedException(DraftAccountNotAllowedException e) {
        log.info("Handling draft account not allowed exception");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, e.getLocalizedMessage());
        details.put(EXCEPTION, DraftAccountNotAllowedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.DRAFT_ACCOUNT_NOT_ALLOWED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    /**
     * FrontofficeReceiptNotFoundException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({FrontofficeReceiptNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleFrontOfficeCommunicationError(FrontofficeReceiptNotFoundException ex) {
        log.info("Error getting receipt from frontoffice");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, ex.getLocalizedMessage());
        details.put(EXCEPTION, FrontofficeReceiptNotFoundException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse("Error getting receipt from frontoffice", HttpStatus.BAD_REQUEST.value(), details);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * FrontofficeServerException
     *
     * @param ex the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({FrontofficeServerException.class})
    public ResponseEntity<ErrorResponse> handleFrontOfficeServerError(FrontofficeServerException ex) {
        log.info("Error getting receipt from frontoffice");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, ex.getLocalizedMessage());
        details.put(EXCEPTION, FrontofficeServerException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse("Frontoffice could not process the request", HttpStatus.INTERNAL_SERVER_ERROR.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({PaymentClientException.class})
    public ResponseEntity<ErrorResponse> handlePaymentClientError(PaymentClientException ex) {
        log.info("Error communicating with external payment platform");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, ex.getLocalizedMessage());
        details.put(EXCEPTION, PaymentClientException.class.getSimpleName());
        ErrorResponse errorResponse = ErrorResponse.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error communicating with external payment platform").details(details).build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * NotVerifiedAccountToReceiveEmailException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({NotVerifiedAccountToReceiveEmailException.class})
    public ResponseEntity<ErrorResponse> handleNotVerifiedAccountToReceiveEmailException(NotVerifiedAccountToReceiveEmailException e) {
        log.info("Handling not verified account not allowed to receive email exception");
        Map<String, String> details = new HashMap<>();
        details.put(MESSAGE, e.getLocalizedMessage());
        details.put(EXCEPTION, NotVerifiedAccountToReceiveEmailException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.NOT_VERIFIED_NOT_ALLOWED_TO_RECEIVE_EMAIL, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * ApplicationEntityLockedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({NoteEntityLockedException.class})
    public ResponseEntity<ErrorResponse> handleNoteEntityLockedException(NoteEntityLockedException e) {
        log.info("Handling note entity locked exception");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, ApplicationEntityLockedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.APPLICATION_ENTITY_LOCKED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * AccountEntityLockedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({AccountEntityLockedException.class})
    public ResponseEntity<ErrorResponse> handleAccountEntityLockedException(AccountEntityLockedException e) {
        log.info("Handling account entity locked exception");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, AccountEntityLockedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.ACCOUNT_ENTITY_LOCKED, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * AccountEntityLockedException
     *
     * @param e the exception
     * @return ErrorResponse
     */
    @ExceptionHandler({PaymentCompletedAnotherUserException.class})
    public ResponseEntity<ErrorResponse> handleAccountEntityLockedException(PaymentCompletedAnotherUserException e) {
        log.info("Handling payment completed by another user exception");
        Map<String, String> details = new HashMap<>();
        details.put(USERNAME, e.getLocalizedMessage());
        details.put(EXCEPTION, AccountEntityLockedException.class.getSimpleName());
        ErrorResponse errorResponse = new ErrorResponse(LiteralConstants.PAYMENT_COMPLETED_ANOTHER_USER, HttpStatus.FORBIDDEN.value(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}
