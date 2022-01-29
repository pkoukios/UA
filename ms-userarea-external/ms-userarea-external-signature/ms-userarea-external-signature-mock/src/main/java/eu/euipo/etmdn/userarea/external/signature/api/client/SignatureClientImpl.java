/*
 * $Id:: SignatureClientImpl.java 2021/04/02 02:17 achristo
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

package eu.euipo.etmdn.userarea.external.signature.api.client;

import eu.euipo.etmdn.userarea.common.domain.SignResponse;
import eu.euipo.etmdn.userarea.common.domain.exception.SignatureClientException;
import eu.euipo.etmdn.userarea.domain.signature.PlatformSignatureDetails;
import eu.euipo.etmdn.userarea.domain.signature.SignRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
@NoArgsConstructor
public class SignatureClientImpl implements SignatureClient {

    @Value("${userarea.signature.platform.url}")
    private String platformUrl;

    @Value("${userarea.signature.platform.createEndpoint}")
    private String createSignatureEndpoint;

    @Value("${userarea.signature.platform.getDetailsEndpoint}")
    private String getSignatureDetailsEndpoint;

    private RestTemplate restTemplate;

    @PostConstruct
    public void setupRestTemplate() {
        this.restTemplate = new RestTemplateBuilder()
                .rootUri(platformUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Connects to external signature platform and receives a signature id
     *
     * @param signRequest the sign request
     * @return String the signature id
     */
    @Override
    public String createSignatureId(SignRequest signRequest) {
        log.info("Requesting id from external signature platform...");
        SignResponse signResponse;
        try {
            signResponse = restTemplate.postForObject(createSignatureEndpoint, signRequest, SignResponse.class);
        } catch (HttpClientErrorException ex) {
            throw new SignatureClientException(ex.getMessage());
        }
        final String signatureId = signResponse != null ? (signResponse).getSignatureId() : null;
        log.info("Get signature id:[{}] from external signature platform...", signatureId);
        return signatureId;
    }

    @Override
    public PlatformSignatureDetails getSignatureDetails(String signatureId) {
        log.info("Requesting signature details from external platform...");
        PlatformSignatureDetails signResponse;
        try {
            signResponse = restTemplate.getForObject(getSignatureDetailsEndpoint + signatureId, PlatformSignatureDetails.class);
        } catch (HttpClientErrorException ex) {
            throw new SignatureClientException(ex.getMessage());
        }
        final String id = signResponse != null ? (signResponse).getSignatureId() : null;
        log.info("Get signature details for the signature ID:[{}] from external signature platform...", id);
        return signResponse;
    }
}
