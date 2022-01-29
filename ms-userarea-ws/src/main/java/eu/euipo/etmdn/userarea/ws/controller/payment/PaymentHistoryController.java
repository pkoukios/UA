/*
 * $Id:: PaymentHistoryController.java 2021/06/27 12:36 dvelegra
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intellectual
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
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchCriteria;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchResult;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentHistorySearchCriteriaResource;
import eu.euipo.etmdn.userarea.ws.domain.payment.PaymentHistorySearchResultResource;
import eu.euipo.etmdn.userarea.ws.mapper.payment.PaymentHistorySearchCriteriaResourceMapper;
import eu.euipo.etmdn.userarea.ws.mapper.payment.PaymentHistorySearchResultResourceMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/payments/history")
@AllArgsConstructor
public class PaymentHistoryController {

    private PaymentService paymentService;
    private PdfService pdfService;

    /**
     * Returns all the user's payment history.
     *
     * @param paymentHistorySearchCriteriaResource the payment history search criteria
     * @return the payment history search results
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS') or hasRole('ROLE_ADMINISTRATOR')")
    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentHistorySearchResultResource> searchPaymentHistory(Authentication authentication, @Valid @RequestBody PaymentHistorySearchCriteriaResource paymentHistorySearchCriteriaResource) {
        PaymentHistorySearchCriteria paymentHistorySearchCriteria = PaymentHistorySearchCriteriaResourceMapper.MAPPER.map(paymentHistorySearchCriteriaResource);
        PaymentHistorySearchResult paymentHistorySearchResult = paymentService.getPaymentHistory(authentication.getName(), paymentHistorySearchCriteria);
        PaymentHistorySearchResultResource paymentHistorySearchResultResource = PaymentHistorySearchResultResourceMapper.MAPPER.map(paymentHistorySearchResult);
        return new ResponseEntity<>(paymentHistorySearchResultResource, HttpStatus.OK);
    }

    /**
     * Downloads the payment details invoice as PDF.
     *
     * @param authentication the authenticated user
     * @param transactionId the transaction id
     * @return the payment details as pdf
     */
    @PreAuthorize("hasRole('ROLE_PAYMENTS') or hasRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/download/invoice/{transactionId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadInvoiceFromHistory(Authentication authentication, @PathVariable("transactionId") String transactionId) {
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
