/*
 * $Id:: PaymentServiceImplTest.java 2021/09/09 05:30 dvelegra
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

package eu.euipo.etmdn.userarea.business.core.impl.service.payment;

import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.ShoppingCartService;
import eu.euipo.etmdn.userarea.common.business.config.ApplicationConfiguration;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.ApplicationSearch;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.constants.LiteralConstants;
import eu.euipo.etmdn.userarea.common.domain.exception.EntityNotFoundException;
import eu.euipo.etmdn.userarea.common.domain.exception.PaymentCompletedAnotherUserException;
import eu.euipo.etmdn.userarea.common.persistence.entity.Application;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentDetails;
import eu.euipo.etmdn.userarea.domain.payment.InitiatePaymentResult;
import eu.euipo.etmdn.userarea.domain.payment.InvoiceDetails;
import eu.euipo.etmdn.userarea.domain.payment.PaymentCallbackResource;
import eu.euipo.etmdn.userarea.domain.payment.PaymentConfirmation;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchCriteria;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchFilterCriteria;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentHistorySearchSort;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatus;
import eu.euipo.etmdn.userarea.domain.payment.PaymentStatusResult;
import eu.euipo.etmdn.userarea.domain.payment.PaymentType;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCart;
import eu.euipo.etmdn.userarea.domain.shoppingcart.ShoppingCartApplication;
import eu.euipo.etmdn.userarea.external.payment.api.client.PaymentClient;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentApplicationEntity;
import eu.euipo.etmdn.userarea.persistence.entity.payment.PaymentEntity;
import eu.euipo.etmdn.userarea.persistence.repository.payment.PaymentApplicationRepository;
import eu.euipo.etmdn.userarea.persistence.repository.payment.PaymentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceImplTest {

    @InjectMocks
    @Spy
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentApplicationRepository paymentApplicationRepository;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private AccountService accountService;
    @Mock
    private ApplicationConfiguration applicationConfiguration;

    private ShoppingCartApplication shoppingCartApplication;

    @Before
    public void setUp() {
        ApplicationSearch applicationSearch = ApplicationSearch.builder()
                .paymentHistory("applicationNumbers,confirmationId,paidBy")
                .build();
        shoppingCartApplication = ShoppingCartApplication.builder().number("123456789").build();
        when(applicationConfiguration.getSearch()).thenReturn(applicationSearch);
    }

    @Test
    public void shouldReturnATransactionIdWhenValidPaymentRequest() {
        String username = "testuser";
        InitiatePaymentDetails paymentRequest = new InitiatePaymentDetails();
        paymentRequest.setApplicationNumbers(Arrays.asList("applicationNumber1", "applicationNumber2"));
        paymentRequest.setPaymentType(PaymentType.CREDIT_CARD.name());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setApplications(Collections.singletonList(shoppingCartApplication));
        shoppingCart.setUser(username);
        String mockTransactionId = "mockTransactionIdTest";
        ShoppingCartApplication mockApplication1 = ShoppingCartApplication.builder()
                .number("applicationNumber1").fees(BigDecimal.valueOf(200.45)).build();
        ShoppingCartApplication mockApplication2 = ShoppingCartApplication.builder()
                .number("applicationNumber2").fees(BigDecimal.valueOf(200.45)).build();
        when(shoppingCartService.getShoppingCartApplicationsByNumbers(any())).thenReturn(Arrays.asList(mockApplication1, mockApplication2));
        when(shoppingCartService.getByUser(eq(username))).thenReturn(shoppingCart);
        when(paymentClient.createTransaction(any())).thenReturn(mockTransactionId);
        when(accountService.getMainAccount(anyString())).thenReturn(DomainAccount.builder().username(username).build());
        InitiatePaymentResult result = paymentService.initiatePayment(username, paymentRequest);
        assertNotNull(result);
        verify(paymentClient, times(1)).createTransaction(any());
        assertEquals(mockTransactionId, result.getTransactionId());
    }

    @Test(expected = PaymentCompletedAnotherUserException.class)
    public void shouldNotInitiatePaymentTransaction() {
        String username = "testuser";
        InitiatePaymentDetails paymentRequest = new InitiatePaymentDetails();
        paymentRequest.setApplicationNumbers(Arrays.asList("12345", "123456789"));
        paymentRequest.setPaymentType(PaymentType.CREDIT_CARD.name());
        when(shoppingCartService.getShoppingCartApplicationsByNumbers(any())).thenReturn(Collections.emptyList());
        InitiatePaymentResult result = paymentService.initiatePayment(username, paymentRequest);
        assertNull(result);
        verify(paymentClient, times(0)).createTransaction(any());
    }

    @Test
    public void shouldReturnATransactionIdWhenChoosingMultipleApplications() {
        String username = "testuser";
        InitiatePaymentDetails paymentRequest = new InitiatePaymentDetails();
        paymentRequest.setApplicationNumbers(Arrays.asList("12345", "123456789"));
        paymentRequest.setPaymentType(PaymentType.CREDIT_CARD.name());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setApplications(Collections.singletonList(shoppingCartApplication));
        shoppingCart.setUser(username);
        String mockTransactionId = "mockTransactionIdTest";
        ShoppingCartApplication mockApplication1 = ShoppingCartApplication.builder()
                .number("applicationNumber1").fees(BigDecimal.valueOf(200.45)).build();
        ShoppingCartApplication mockApplication2 = ShoppingCartApplication.builder()
                .number("applicationNumber2").fees(BigDecimal.valueOf(200.45)).build();
        when(shoppingCartService.getShoppingCartApplicationsByNumbers(any())).thenReturn(Arrays.asList(mockApplication1, mockApplication2));
        when(shoppingCartService.getByUser(eq(username))).thenReturn(shoppingCart);
        when(paymentClient.createTransaction(any())).thenReturn(mockTransactionId);
        when(accountService.getMainAccount(anyString())).thenReturn(DomainAccount.builder().username(username).build());
        InitiatePaymentResult result = paymentService.initiatePayment(username, paymentRequest);
        assertNotNull(result);
        verify(paymentClient, times(1)).createTransaction(any());
        assertEquals(mockTransactionId, result.getTransactionId());
    }

    @Test
    public void shouldCheckPaymentStatusAndReturnValidWhenStatusPaid() {
        String mockTransactionId = "mockTransactionId";
        String applicationNumbers = "number1,number2";
        String username = "test@test.com";
        PaymentEntity paymentEntity = PaymentEntity.builder().paymentReference("user entered text").paidBy(username).status(PaymentStatus.PAID)
                .transactionId(mockTransactionId).applicationNumbers(applicationNumbers).total(BigDecimal.valueOf(234.8)).build();
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.of(paymentEntity));
        PaymentStatusResult result = paymentService.checkStatus(mockTransactionId);
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertEquals(mockTransactionId, result.getTransactionId());
    }

    @Test
    public void shouldCheckPaymentStatusAndReturnValidWhenStatusPAID_UPDATE_FO_FAILED() {
        String mockTransactionId = "mockTransactionId";
        String applicationNumbers = "number1,number2";
        String username = "test@test.com";
        PaymentEntity paymentEntity = PaymentEntity.builder().paymentReference("user entered text").paidBy(username).status(PaymentStatus.PAID_UPDATE_FO_FAILED)
                .transactionId(mockTransactionId).applicationNumbers(applicationNumbers).total(BigDecimal.valueOf(234.8)).build();
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.of(paymentEntity));
        PaymentStatusResult result = paymentService.checkStatus(mockTransactionId);
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        assertEquals(mockTransactionId, result.getTransactionId());
    }

    @Test
    public void shouldCheckPaymentStatusAndReturnInvalidWhenPaymentHasFailed() {
        String mockTransactionId = "mockTransactionId";
        String applicationNumbers = "number1,number2";
        String username = "test@test.com";
        String paymentPlatformErrorMessage = "Error from payment platform. Payment failed";
        PaymentEntity paymentEntity = PaymentEntity.builder().paymentReference("user entered text").paidBy(username).status(PaymentStatus.FAILED)
                .transactionId(mockTransactionId).errorMessage(paymentPlatformErrorMessage).applicationNumbers(applicationNumbers).total(BigDecimal.valueOf(234.8)).build();
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.of(paymentEntity));
        PaymentStatusResult result = paymentService.checkStatus(mockTransactionId);
        assertFalse(result.isValid());
        assertEquals(paymentPlatformErrorMessage, result.getErrorMessage());
        assertEquals(LiteralConstants.PAYMENT_NOT_COMPLETE_DUE_TO, result.getErrorCode());
        assertEquals(mockTransactionId, result.getTransactionId());
    }

    @Test
    public void shouldThrowExceptionWhenPaymentIsNotFound() {
        String mockTransactionId = "mockTransactionId";
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            paymentService.checkStatus(mockTransactionId);
        });
        String expectedMessage = "Could not find payment with transaction: " + mockTransactionId;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void shouldConfirmPaymentWhenCallbackIsValid() {
        String mockTransactionId = "mockTransactionId";
        String applicationNumbers = "number1,number2";
        String username = "test@test.com";
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .paymentApplications(Collections.singletonList(PaymentApplicationEntity.builder().applicationId(1L).build()))
                .paymentReference("user entered text").paidBy(username).status(PaymentStatus.PAID).applicationIds("1,2")
                .transactionId(mockTransactionId).applicationNumbers(applicationNumbers).total(BigDecimal.valueOf(234.8)).build();
        PaymentCallbackResource callbackResource = PaymentCallbackResource.builder().status("PAID").transactionId(mockTransactionId)
                .confirmationId("testid").paidAt(LocalDateTime.now()).build();
        Application mockApplication = Application.builder()
                .number("applicationNumber1").foModule(ApplicationType.TRADEMARK.value).fees(BigDecimal.valueOf(200.45)).build();
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.of(paymentEntity));
        when(applicationService.getApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication));
        paymentService.confirm(callbackResource);
        verify(paymentRepository, times(1)).save(eq(paymentEntity));
        verify(paymentService, times(1)).asyncNotifyFrontoffice(eq(paymentEntity));
    }

    @Test
    public void shouldNotConfirmPaymentWhenPaymentDoesNotExist() {
        String mockTransactionId = "mockTransactionId";
        PaymentCallbackResource callbackResource = PaymentCallbackResource.builder().status("PAID").transactionId(mockTransactionId)
                .confirmationId("testid").paidAt(LocalDateTime.now()).build();
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.empty());
        paymentService.confirm(callbackResource);
        verify(paymentRepository, times(0)).save(any());
        verify(paymentService, times(0)).asyncNotifyFrontoffice(any());
    }

    @Test
    public void shouldThrowExceptionWhenTransactionIdIsNotFound() {
        String mockTransactionId = "mockTransactionId";
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            paymentService.getConfirmation(mockTransactionId);
        });
        String expectedMessage = "Could not find a transaction with id: " + mockTransactionId;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenStatusIsNotPaid() {
        String mockTransactionId = "mockTransactionId";
        String applicationNumber = "number1";
        String username = "test@test.com";
        PaymentEntity paymentEntity = PaymentEntity.builder().paymentReference("user entered text").paidBy(username).status(PaymentStatus.FAILED)
                .transactionId(mockTransactionId).cartId(111L).applicationNumbers(applicationNumber).total(BigDecimal.valueOf(234.8)).build();
        when(paymentRepository.findByTransactionId(eq(mockTransactionId))).thenReturn(Optional.of(paymentEntity));
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            paymentService.getConfirmation(mockTransactionId);
        });
        String expectedMessage = "Payment is not successful. You cannot view the confirmation page";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void shouldReturnAConfirmationWhenTransactionIsPaid() {
        String mockTransactionId = "mockTransactionIdTest";
        String mockConfirmationId = "mockConfirmationId";
        String username = "testuser";
        String applicationNumbers = "applicationNumbers1";
        InitiatePaymentDetails paymentRequest = new InitiatePaymentDetails();
        paymentRequest.setPaymentType(PaymentType.CREDIT_CARD.name());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setApplications(Collections.emptyList());
        shoppingCart.setUser(username);
        ShoppingCartApplication mockApplication1 = ShoppingCartApplication.builder()
                .number("applicationNumber1").fees(BigDecimal.valueOf(200.45)).build();
        ShoppingCartApplication mockApplication2 = ShoppingCartApplication.builder()
                .number("applicationNumber2").fees(BigDecimal.valueOf(200.45)).build();
        ShoppingCartApplication mockApplication3 = ShoppingCartApplication.builder()
                .number("applicationNumber3").fees(BigDecimal.valueOf(200.45)).build();
        ShoppingCartApplication mockApplication4 = ShoppingCartApplication.builder()
                .number("applicationNumber4").fees(BigDecimal.valueOf(200.45)).build();
        PaymentEntity paymentEntity = PaymentEntity.builder().paymentReference("user entered text")
                .paymentApplications(Collections.singletonList(PaymentApplicationEntity.builder().applicationId(1L).build()))
                .paidBy(username)
                .applicationIds("1,2,3,4,5")
                .status(PaymentStatus.PAID).confirmationId(mockConfirmationId)
                .transactionId(mockTransactionId).cartId(123L)
                .applicationNumbers(applicationNumbers).total(BigDecimal.valueOf(200.45)).build();
        Application mockApplication = Application.builder()
                .number("applicationNumber1").foModule(ApplicationType.TRADEMARK.value).fees(BigDecimal.valueOf(200.45)).build();
        when(applicationService.getApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication));
        when(paymentRepository.findByTransactionId(anyString())).thenReturn(Optional.of(paymentEntity));
        //ApplicationType.TRADEMARK.value
        when(shoppingCartService.getShoppingCartApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication1));
        PaymentConfirmation paymentConfirmation = paymentService.getConfirmation(mockTransactionId);
        //ApplicationType.DESIGN.value
        when(shoppingCartService.getShoppingCartApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication2));
        paymentService.getConfirmation(mockTransactionId);
        //ApplicationType.ESERVICE.value ,SearchMessageType.TRADEMARKS
        when(shoppingCartService.getShoppingCartApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication3));
        paymentService.getConfirmation(mockTransactionId);
        //ApplicationType.ESERVICE.value ,SearchMessageType.DESIGNS
        when(shoppingCartService.getShoppingCartApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication4));
        paymentService.getConfirmation(mockTransactionId);
        assertNotNull(paymentConfirmation);
        assertEquals(mockConfirmationId, paymentConfirmation.getConfirmationId());
    }

    @Test
    public void shouldReturnInvoiceWhenTransactionIsPaid() {
        String mockTransactionId = "mockTransactionIdTest";
        String mockConfirmationId = "mockConfirmationId";
        String username = "testuser";
        String applicationNumbers = "applicationNumbers1";
        Application mockApplication1 = Application.builder()
                .number("applicationNumber1").foModule(ApplicationType.TRADEMARK.value).fees(BigDecimal.valueOf(200.45)).build();
        PaymentEntity paymentEntity = PaymentEntity.builder().paymentReference("user entered text")
                .paymentApplications(Collections.singletonList(PaymentApplicationEntity.builder().applicationId(1L).number("123").build()))
                .paidBy(username)
                .applicationIds("1,2,3,4,5")
                .status(PaymentStatus.PAID).confirmationId(mockConfirmationId)
                .transactionId(mockTransactionId).cartId(123L)
                .applicationNumbers(applicationNumbers).total(BigDecimal.valueOf(200.45)).build();
        when(paymentRepository.findByTransactionId(anyString())).thenReturn(Optional.of(paymentEntity));
        //ApplicationType.TRADEMARK.value
        when(applicationService.getApplicationsByIds(anyList())).thenReturn(Collections.singletonList(mockApplication1));
        InvoiceDetails invoiceDetails = paymentService.getInvoice(mockTransactionId);
        assertNotNull(invoiceDetails);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotReturnInvoiceWhenEntityNotFound() {
        String mockTransactionId = "mockTransactionIdTest";
        when(paymentRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        paymentService.getInvoice(mockTransactionId);
    }

    @Test
    public void testGetPaymentHistory() {
        final String username = "test@abc.com";
        DomainAccount domainAccount = DomainAccount.builder().username(username).build();
        PaymentHistorySearchCriteria criteria = new PaymentHistorySearchCriteria();
        criteria.setSortType("ASC");
        criteria.setRequestPage(1);
        criteria.setSize(10);
        criteria.setSort(PaymentHistorySearchSort.PAYMENT_ID);
        criteria.setFilterCriteria(new PaymentHistorySearchFilterCriteria());
        criteria.setSearch("test");
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .id(1L)
                .applicationNumbers("EM2021001,EM2021002,EM2021003")
                .paidBy(username)
                .paymentReference("test payment history")
                .transactionId("12345")
                .confirmationId("6789")
                .updatedAt(LocalDateTime.now())
                .build();
        Page<PaymentEntity> pagedResponse = new PageImpl(Collections.singletonList(paymentEntity));
        when(accountService.getMainAccount(username)).thenReturn(domainAccount);
        when(paymentRepository.findAll(any(Specification.class),any(Pageable.class))).thenReturn(pagedResponse);
        PaymentHistorySearchResult result = paymentService.getPaymentHistory(username, criteria);
        assertNotNull(result);
    }

    @Test
    public void testGetPaymentApplicationByApplicationId() {
        PaymentApplicationEntity paymentApplicationEntity = PaymentApplicationEntity.builder().applicationId(1L).number("123").build();
        when(paymentApplicationRepository.findByApplicationId(1L)).thenReturn(paymentApplicationEntity);
        PaymentApplicationEntity paymentApplication = paymentService.getPaymentApplicationByApplicationId(1L);
        assertNotNull(paymentApplication);
        verify(paymentApplicationRepository,times(1)).findByApplicationId(anyLong());
    }

    @Test
    public void testGetPaymentApplicationByApplicationNumber() {
        final String number = "12345";
        PaymentApplicationEntity paymentApplicationEntity = PaymentApplicationEntity.builder().applicationId(1L).number(number).build();
        when(paymentApplicationRepository.findByNumber(number)).thenReturn(Collections.singletonList(paymentApplicationEntity));
        List<PaymentApplicationEntity> paymentApplications = paymentService.getPaymentApplicationByApplicationNumber(number);
        assertNotNull(paymentApplications);
        assertFalse(paymentApplications.isEmpty());
        verify(paymentApplicationRepository,times(1)).findByNumber(anyString());
    }

}