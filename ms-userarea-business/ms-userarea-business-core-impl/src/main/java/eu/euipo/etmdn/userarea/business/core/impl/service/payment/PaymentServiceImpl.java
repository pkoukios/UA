/*
 * $Id:: PaymentServiceImpl.java 2021/05/11 01:48 achristo
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

package eu.euipo.etmdn.userarea.business.core.impl.service.payment;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.business.core.impl.mapper.ApplicationMapper;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.exception.EntityNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.PaymentCompletedAnotherUserException;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.payment.FOPaymentResource;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentResult;
import eu.euipo.etmdn.userarea.domain.payment.InvoiceDetails;
import eu.euipo.etmdn.userarea.domain.payment.PaidApplication;
import eu.euipo.etmdn.userarea.domain.payment.PaymentCallbackResource;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmation;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearch;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchCriteria;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatus;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatusResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentType;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.external.payment.api.client.PaymentClient;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import eu.euipo.etmdn.userarea.persistence.mapper.payment.PaymentHistorySearchMapper;
import eu.euipo.etmdn.userarea.persistence.mapper.payment.PaymentResourceMapper;
import eu.euipo.etmdn.userarea.persistence.repository.payment.PaymentApplicationRepository;
import eu.euipo.etmdn.userarea.persistence.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.euipo.etmdn.userarea.business.core.impl.utils.PaymentUtils.getPaymentHistoryFilterSpecificationPredicates;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * Payment business implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentApplicationRepository paymentApplicationRepository;
    private final ApplicationService applicationService;
    private final AccountService accountService;
    private final PaymentClient paymentClient;
    private final ShoppingCartService shoppingCartService;
    private final ApplicationConfiguration applicationConfiguration;
    private final RestTemplate restTemplate;

    @Value("${userarea.globals.ipo.fo.url}")
    private String frontofficeUrl;
    @Value("${userarea.payment.platform.url}")
    private String platformUrl;
    @Value("${userarea.payment.platform.createEndpoint}")
    private String platformCreateEndpoint;
    @Value("${userarea.payment.frontoffice.updateEndpoint}")
    private String foPaymentUpdateEndpoint;

    /**
     * Retrieves the payment history of a user.
     *
     * @param username the username
     * @param paymentHistorySearchCriteria the payment history search criteria
     * @return {@link PaymentHistorySearchResult} the payment history results
     */
    @Override
    public PaymentHistorySearchResult getPaymentHistory(final String username, PaymentHistorySearchCriteria paymentHistorySearchCriteria) {
        log.info("Retrieve payment history for user:{} by search criteria {}", username, paymentHistorySearchCriteria);
        final String parentUsername = accountService.getMainAccount(username).getUsername();
        List<PaymentHistorySearch> paymentHistorySearchResultContents = new ArrayList<>();
        Sort.Direction direction = paymentHistorySearchCriteria.getSortType().equalsIgnoreCase("ASC")? Sort.Direction.ASC: Sort.Direction.DESC;
        Pageable page = PageRequest.of(paymentHistorySearchCriteria.getRequestPage(), paymentHistorySearchCriteria.getSize(),
                Sort.by(direction, paymentHistorySearchCriteria.getSort().toString()));
        Specification<PaymentEntity> paymentEntitySpecifications = getPaymentHistoryFilterSpecificationPredicates(parentUsername,
                paymentHistorySearchCriteria.getFilterCriteria(), paymentHistorySearchCriteria.getSearch(),
                applicationConfiguration.getSearch().getPaymentHistory());
        Page<PaymentEntity> paymentEntities = paymentRepository.findAll(paymentEntitySpecifications, page);
        for (PaymentEntity paymentEntity : paymentEntities) {
            PaymentHistorySearch paymentHistorySearch = PaymentHistorySearchMapper.MAPPER.map(paymentEntity);
            paymentHistorySearchResultContents.add(paymentHistorySearch);
        }
        PaymentHistorySearchResult paymentHistorySearchResult = new PaymentHistorySearchResult();
        paymentHistorySearchResult.setContent(paymentHistorySearchResultContents);
        paymentHistorySearchResult.setPageNumber(paymentEntities.getNumber());
        paymentHistorySearchResult.setPageSize(paymentEntities.getSize());
        paymentHistorySearchResult.setTotalPages(paymentEntities.getTotalPages());
        paymentHistorySearchResult.setTotalResults(paymentEntities.getTotalElements());
        return paymentHistorySearchResult;
    }

    /**
     * Retrieve payment transaction details by application id.
     *
     * @param id the application id
     * @return {@link PaymentApplicationEntity} the payment application details
     */
    @Override
    public PaymentApplicationEntity getPaymentApplicationByApplicationId(Long id) {
        return paymentApplicationRepository.findByApplicationId(id);
    }

    /**
     * Retrieve payment transaction details by application number.
     *
     * @param applicationNumber the application number
     * @return {@link PaymentApplicationEntity} the payment application details
     */
    @Override
    public List<PaymentApplicationEntity> getPaymentApplicationByApplicationNumber(String applicationNumber) {
        return paymentApplicationRepository.findByNumber(applicationNumber);
    }

    /**
     * Retrieve current data about a payment transaction.
     *
     * @param transactionId the unique payment transaction id provided by the external platform
     * @return a payment confirmation response
     */
    @Override
    public PaymentConfirmation getConfirmation(String transactionId) {
        log.info("Getting payment confirmation details for transaction id: {}", transactionId);
        Optional<PaymentEntity> paymentOptional = paymentRepository.findByTransactionId(transactionId);
        if (!paymentOptional.isPresent()) {
            throw new EntityNotFoundException("Could not find a transaction with id: " + transactionId);
        }
        PaymentEntity paymentEntity = paymentOptional.get();
        if (!Arrays.asList(PaymentStatus.PAID, PaymentStatus.PAID_UPDATE_FO_FAILED).contains(paymentEntity.getStatus())) {
            throw new IllegalStateException("Payment is not successful. You cannot view the confirmation page");
        }
        List<Long> applicationIdList = Stream.of(paymentEntity.getApplicationIds().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        List<ShoppingCartApplication> shoppingCartApplications = shoppingCartService.getShoppingCartApplicationsByIds(applicationIdList);
        log.info("Payment has status {}, total: {}, applications: {}", paymentEntity.getStatus(), paymentEntity.getTotal(), paymentEntity.getApplicationNumbers());
        List<PaymentApplicationEntity> paymentApplicationEntityList = new ArrayList<>();
        PaymentApplicationEntity paymentApplicationEntity;
        for (ShoppingCartApplication application : shoppingCartApplications) {
            paymentApplicationEntity = PaymentApplicationEntity.builder()
                    .payment(paymentEntity)
                    .applicationId(application.getApplicationId())
                    .number(application.getNumber())
                    .ipRightType(application.getType())
                    .build();
            paymentApplicationEntityList.add(paymentApplicationEntity);
        }
        paymentApplicationRepository.saveAll(paymentApplicationEntityList);
        List<ShoppingCartApplication> groupedByNumberShoppingCartApplications = shoppingCartApplications.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(ShoppingCartApplication::getNumber))), ArrayList::new));
        updateApplicationStatusAndShoppingCart(paymentEntity);
        return PaymentConfirmation.builder()
                .applications(groupedByNumberShoppingCartApplications)
                .confirmationId(paymentEntity.getConfirmationId())
                .applicationsCount(groupedByNumberShoppingCartApplications.size())
                .paidBy(paymentEntity.getPaidBy())
                .dateTimeOfSubmission(paymentEntity.getSubmissionDateTime())
                .paymentMethod(paymentEntity.getType())
                .paymentReference(paymentEntity.getPaymentReference())
                .paymentStatus(paymentEntity.getStatus())
                .total(paymentEntity.getTotal().toString())
                .transactionId(paymentEntity.getTransactionId())
                .build();
    }

    /**
     * Retrieve current data about a payment transaction.
     *
     * @param transactionId the unique payment transaction id provided by the external platform
     * @return a payment confirmation response
     */
    @Override
    public InvoiceDetails getInvoice(String transactionId) {
        log.info("Getting payment details for the invoice of the transaction id: {}", transactionId);
        Optional<PaymentEntity> paymentOptional = paymentRepository.findByTransactionId(transactionId);
        if (!paymentOptional.isPresent()) {
            throw new EntityNotFoundException("Could not find a transaction with id: " + transactionId);
        }
        PaymentEntity paymentEntity = paymentOptional.get();
        List<PaymentApplicationEntity> groupedByNumberPaymentApplications = paymentEntity.getPaymentApplications().stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(PaymentApplicationEntity::getNumber))), ArrayList::new));
        List<Long> applicationIdList = groupedByNumberPaymentApplications.stream()
                .map(PaymentApplicationEntity::getApplicationId)
                .collect(Collectors.toList());
        List<Application> applications = applicationService.getApplicationsByIds(applicationIdList);
        List<PaidApplication> paidApplications = ApplicationMapper.MAPPER.toPaidApplications(applications);
        return InvoiceDetails.builder()
                .applications(paidApplications)
                .confirmationId(paymentEntity.getConfirmationId())
                .applicationsCount(applicationIdList.size())
                .paidBy(paymentEntity.getPaidBy())
                .dateTimeOfSubmission(paymentEntity.getSubmissionDateTime())
                .paymentMethod(paymentEntity.getType())
                .paymentReference(paymentEntity.getPaymentReference())
                .paymentStatus(paymentEntity.getStatus())
                .total(paymentEntity.getTotal().toString())
                .transactionId(paymentEntity.getTransactionId())
                .build();
    }

    /**
     * Initializes a new payment request to the external payment platform and retrieves a unique transaction id.
     * This method will setup the payment information so that the user when is redirected
     * to pay the platform know the details such as the shopping cart identifier and the total amount.
     *
     * @param initiatePaymentDetails the payment details to be sent to the external payment platform
     * @return a Payment response with unique identifier for the transaction. It will be used to form the redirection url
     */
    @Override
    public InitiatePaymentResult initiatePayment(String username, InitiatePaymentDetails initiatePaymentDetails) {
        log.info("Initiate a payment transaction for application numbers:[{}]", String.join(",", initiatePaymentDetails.getApplicationNumbers()));
        List<ShoppingCartApplication> shoppingCartApplications = shoppingCartService.getShoppingCartApplicationsByNumbers(initiatePaymentDetails.getApplicationNumbers());
        if (CollectionUtils.isEmpty(shoppingCartApplications)) {
            throw new PaymentCompletedAnotherUserException("");
        }
        List<Long> applicationIdList = shoppingCartApplications.stream()
                .map(ShoppingCartApplication::getApplicationId)
                .collect(Collectors.toList());
        List<ShoppingCartApplication> groupedByNumberShoppingCartApplications = shoppingCartApplications.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(ShoppingCartApplication::getNumber))), ArrayList::new));
        // Calculate total amount to be paid
        BigDecimal totalAmount = groupedByNumberShoppingCartApplications.stream().map(ShoppingCartApplication::getFees).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total amount {} for applications {}", totalAmount, String.join(",", initiatePaymentDetails.getApplicationNumbers()));
        initiatePaymentDetails.setTotalAmount(totalAmount.doubleValue());
        log.info("Locking applications");
        List<Application> applicationList = applicationIdList.stream()
                .map(id -> applicationService.getByIdAndLock(id, username))
                .collect(Collectors.toList());
        applicationService.saveAll(applicationList);
        log.info("Requesting transaction Id from payment platform...");
        String transactionId = paymentClient.createTransaction(initiatePaymentDetails);
        log.info("Retrieved transaction id: {} from external payment platform", transactionId);
        String parent = accountService.getMainAccount(username).getUsername();
        ShoppingCart shoppingCart = shoppingCartService.getByUser(parent);
        // Save transaction to db
        log.info("Saving new payment transaction with status PENDING to DB");
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .owner(shoppingCart != null ? shoppingCart.getUser() : parent)
                .type(PaymentType.valueOf(initiatePaymentDetails.getPaymentType()))
                .paymentReference(initiatePaymentDetails.getReference())
                .applicationIds(StringUtils.join(applicationIdList, ','))
                .applicationNumbers(String.join(",", initiatePaymentDetails.getApplicationNumbers()))
                .status(PaymentStatus.PENDING)
                .paidBy(username)
                .total(totalAmount)
                .transactionId(transactionId)
                .cartId(shoppingCart != null ? shoppingCart.getId() : null)
                .build();
        paymentRepository.save(paymentEntity);
        log.info("Payment transaction details saved to db");

        return InitiatePaymentResult.builder()
                .transactionId(transactionId)
                .redirectToPaymentPlatformUrl(getRedirectToExternalPlatformUrl(transactionId))
                .build();
    }

    /**
     * Check the status of a payment in the local database. The payment status is updated from the
     * callback of the payment platform and the results are written in userarea database.
     *
     * @param transactionId the unique identifier of a payment shared between the payment platform and userarea
     * @return a {@link PaymentStatusResult} with details about the payment
     */
    @Override
    public PaymentStatusResult checkStatus(String transactionId) {
        log.info("Checking payment status for transaction id: {}", transactionId);
        Optional<PaymentEntity> paymentEntityOptional = paymentRepository.findByTransactionId(transactionId);
        if (!paymentEntityOptional.isPresent()) {
            String errorMessage = "Could not find payment with transaction: " + transactionId;
            log.error(errorMessage);
            throw new EntityNotFoundException(errorMessage);
        }
        PaymentEntity paymentEntity = paymentEntityOptional.get();
        log.info(">>> Transaction {} status is {}", transactionId, paymentEntity.getStatus());
        PaymentStatusResult result = PaymentResourceMapper.MAPPER.map(paymentEntity);
        result.setValid(result.getStatus().equals(PaymentStatus.PAID.name()) || result.getStatus().equals(PaymentStatus.PAID_UPDATE_FO_FAILED.name()));
        log.info("Transaction validation status is {}", result.isValid());
        if (paymentEntity.getErrorMessage() != null) {
            log.info(">>> Error from external payment platform: <{}>", paymentEntity.getErrorMessage());
            result.setErrorCode(LiteralConstants.PAYMENT_NOT_COMPLETE_DUE_TO);
            result.setErrorMessage(paymentEntity.getErrorMessage());
        }
        return result;
    }

    /**
     * Saves to userarea db the status of a transaction. Called from the payment platform to notify userarea
     *
     * @param paymentCallbackResource details about the payment transaction
     */
    @Override
    public void confirm(PaymentCallbackResource paymentCallbackResource) {
        log.info("Callback from payment platform: transactionId: {} signingId: {} status: {}",
                paymentCallbackResource.getTransactionId(), paymentCallbackResource.getConfirmationId(), paymentCallbackResource.getStatus());
        Optional<PaymentEntity> paymentEntityOptional = paymentRepository.findByTransactionId(paymentCallbackResource.getTransactionId());
        if (paymentEntityOptional.isPresent() && paymentEntityOptional.get().getApplicationNumbers() != null) {
            PaymentEntity paymentEntity = paymentEntityOptional.get();
            // Update payment entity status
            paymentEntity.setStatus(PaymentStatus.valueOf(paymentCallbackResource.getStatus()));
            paymentEntity.setConfirmationId(paymentCallbackResource.getConfirmationId());
            paymentEntity.setSubmissionDateTime(paymentCallbackResource.getPaidAt());
            paymentEntity.setErrorMessage(paymentCallbackResource.getErrorMessage());
            paymentRepository.save(paymentEntity);
            asyncNotifyFrontoffice(paymentEntity);
            // Release lock for applications after confirmation from external platform
            List<Long> applicationIdList = Stream.of(paymentEntity.getApplicationIds().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Application> applications = applicationService.getApplicationsByIds(applicationIdList);
            applicationService.updateAndReleaseApplicationsLock(applications, paymentEntity.getPaidBy());
        } // End of entity null check
    }


    protected void updateApplicationStatusAndShoppingCart(PaymentEntity paymentEntity) {
        if (paymentEntity.getStatus().equals(PaymentStatus.PAID) || paymentEntity.getStatus().equals(PaymentStatus.PAID_UPDATE_FO_FAILED)) {
            // Update each application status to Submitted
            List<Long> applicationIdList = Stream.of(paymentEntity.getApplicationIds().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Application> applications = applicationService.getApplicationsByIds(applicationIdList);
            applications.forEach(application -> {
                LocalDateTime now = LocalDateTime.now();
                log.info("Updating application {} status to submitted", application.getNumber());
                application.setStatus("Submitted");
                application.setLastModifiedDate(now);
                application.setStatusDate(now);
                application.setApplicationDate(now);
                applicationService.save(application);
                removeApplicationFromShoppingCart(application.getId());
            });
        }
    }

    protected void removeApplicationFromShoppingCart(Long applicationId) {
        // Remove the application from shopping cart
        log.info("Updating shopping cart by removing paid application");
        shoppingCartService.removeApplication(applicationId);
        log.info("Removed application {} from shopping cart", applicationId);
    }

    protected void asyncNotifyFrontoffice(PaymentEntity paymentEntity) {
        if (paymentEntity.getStatus().equals(PaymentStatus.PAID) || paymentEntity.getStatus().equals(PaymentStatus.PAID_UPDATE_FO_FAILED)) {
            // Notify frontoffice about the new status
            CompletableFuture
                    .supplyAsync(() -> notifyPaymentToFrontoffice(paymentEntity))
                    .thenAccept(result -> {
                        // If we cannot notify frontoffice update signature status (the status is considered completed)
                        if (!result) {
                            paymentEntity.setStatus(PaymentStatus.PAID_UPDATE_FO_FAILED);
                            paymentRepository.save(paymentEntity);
                        }
                    });
        }
    }

    private boolean notifyPaymentToFrontoffice(PaymentEntity paymentEntity) {
        FOPaymentResource payload = new FOPaymentResource();
        List<String> applicationNumbers = Arrays.asList(paymentEntity.getApplicationNumbers().split(","));
        payload.setFillingNumbers(applicationNumbers);
        payload.setPaymentKind(paymentEntity.getType().getValue());
        payload.setStatus("5100"); // Frontoffice code for Submitted status
        // Update signature in frontoffice
        try {
            log.info(">>> Notifying payment to FrontOffice for applicationNumbers: " + applicationNumbers);
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            restTemplate.postForObject(frontofficeUrl + foPaymentUpdateEndpoint, payload, String.class);
        } catch (RestClientException ex) {
            log.error(">>> Could not notify new payment to FrontOffice: " + ex.getLocalizedMessage());
            return false;
        }
        log.info(">>> Payment to FrontOffice has been updated");
        return true;
    }

    private String getRedirectToExternalPlatformUrl(String transactionId) {
        return String.join("/", platformUrl, "payments", transactionId);
    }
}
