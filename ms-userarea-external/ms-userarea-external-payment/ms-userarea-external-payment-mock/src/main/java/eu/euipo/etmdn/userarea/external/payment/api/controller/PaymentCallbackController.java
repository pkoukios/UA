/*
 * $Id:: Callback.java 2021/04/05 10:03 achristo
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

package eu.euipo.etmdn.userarea.external.payment.api.controller;

import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.domain.payment.PaymentCallbackResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments/callback")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody PaymentCallbackResource paymentCallbackResource) {

        paymentService.confirm(paymentCallbackResource);

        return ResponseEntity.ok().build();
    }
}