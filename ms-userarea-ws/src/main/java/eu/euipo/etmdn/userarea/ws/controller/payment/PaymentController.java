/*
 * $Id:: PaymentController.java 2021/04/27 12:36 achristo
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

package eu.euipo.etmdn.userarea.ws.controller.payment;

import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmation;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmationResponse;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatusResult;
import eu.euipo.etmdn.userarea.ws.domain.payment.InitiatePaymentRequest;
import eu.euipo.etmdn.userarea.ws.domain.payment.InitiatePaymentResponse;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentStatusResponse;
import eu.euipo.etmdn.userarea.ws.mapper.payment.PaymentMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentController {

    private PaymentService paymentService;
    private PdfService pdfService;

    /**
     * Returns the payment initiation details
     *
     * @param paymentRequest the payment details
     * @return a {@link InitiatePaymentResponse}
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(Authentication authentication, @RequestBody @Valid InitiatePaymentRequest paymentRequest) {
        log.info("Initiating payment for user {}", authentication.getName());
        InitiatePaymentResult initiatePaymentResult = paymentService.initiatePayment(authentication.getName(), PaymentMapper.MAPPER.map(paymentRequest));
        return ResponseEntity.ok(PaymentMapper.MAPPER.map(initiatePaymentResult));
    }

    @PreAuthorize("hasRole('ROLE_PAYMENTS') or hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/checkStatus/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentStatusResponse> checkStatus(@PathVariable("transactionId") String transactionId) {
        PaymentStatusResult paymentStatusResult = paymentService.checkStatus(transactionId);
        return ResponseEntity.ok(PaymentMapper.MAPPER.map(paymentStatusResult));
    }

    @PreAuthorize("hasRole('ROLE_PAYMENTS') or hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/confirmation/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentConfirmationResponse> confirmation(Authentication authentication, @PathVariable("transactionId") String transactionId) {
        PaymentConfirmation paymentConfirmation = paymentService.getConfirmation(transactionId);
        PaymentConfirmationResponse response = PaymentMapper.MAPPER.map(paymentConfirmation);
        response.setApplications(paymentConfirmation.getApplications());
        response.setPaymentMethod(paymentConfirmation.getPaymentMethod().getValue());
        response.setPaidBy(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Downloads the payment details invoice as PDF.
     *
     * @param authentication the authenticated user
     * @param transactionId the transaction id
     * @return the payment details as pdf
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS') or hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/invoice/{transactionId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getInvoice(Authentication authentication, @PathVariable("transactionId") String transactionId) {
        log.info("Generating invoice in PDF format for payment details {}", transactionId);
        FileInfo fileInfo = pdfService.generatePdf(authentication.getName(), transactionId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                .contentLength(fileInfo.getFileContent().length)
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(fileInfo.getFileContent());
    }

}
