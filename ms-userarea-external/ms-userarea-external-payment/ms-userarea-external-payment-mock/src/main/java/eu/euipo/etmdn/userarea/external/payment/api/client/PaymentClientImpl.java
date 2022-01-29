/*
 * $Id:: PaymentClientImpl.java 2021/05/13 03:41 achristo
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

package eu.euipo.etmdn.userarea.external.payment.api.client;

import eu.euipo.etmdn.userarea.common.domain.payment.PayResponse;
import eu.euipo.etmdn.userarea.common.domain.exception.PaymentClientException;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Service
@NoArgsConstructor
public class PaymentClientImpl implements PaymentClient {

    @Value("${userarea.payment.platform.url}")
    private String platformUrl;

    @Value("${userarea.payment.callback.url}")
    private String callbackUrl;

    @Value("${userarea.payment.platform.createEndpoint}")
    private String createPaymentEndpoint;

    @Value("${userarea.payment.platform.getDetailsEndpoint}")
    private String getPaymentDetailsEndpoint;

    private RestTemplate restTemplate;

    @PostConstruct
    public void setupRestTemplate() {
        this.restTemplate = new RestTemplateBuilder()
                .rootUri(platformUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    /**
     * Connect to external payment platform and create a transaction id
     *
     * @param initiatePaymentDetails the payment request details
     * @return a unique transaction id
     */
    @Override
    public String createTransaction(InitiatePaymentDetails initiatePaymentDetails) {
        log.info("Requesting transaction id from external payment platform...");
        initiatePaymentDetails.setCallbackUrl(callbackUrl);
        PayResponse payResponse;
        try {
            payResponse = restTemplate.postForObject(createPaymentEndpoint, initiatePaymentDetails, PayResponse.class);
        } catch (RestClientException ex) {
            log.error("Error retrieving a transaction id from external payment platform");
            log.error(ex.getLocalizedMessage());
            throw new PaymentClientException("Error retrieving a transaction id from external payment platform");
        }
        final String transactionId = payResponse != null ? payResponse.getTransactionId() : null;
        log.info("Get payment id:[{}] from external payment platform...", transactionId);
        return transactionId;
    }
}
