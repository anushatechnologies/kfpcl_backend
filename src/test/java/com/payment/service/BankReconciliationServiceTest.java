package com.payment.service;

import com.payment.dto.webhook.BankReconciliationWebhookRequest;
import com.payment.dto.webhook.BankReconciliationWebhookResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.WebhookEvent;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.InvoiceType;
import com.payment.entity.enums.PaymentStatus;
import com.payment.exception.BadRequestException;
import com.payment.exception.DuplicateResourceException;
import com.payment.exception.UnauthorizedException;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.repository.WebhookEventRepository;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.BankReconciliationServiceImpl;
import com.payment.service.state.PaymentStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankReconciliationServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private PaymentStateMachine stateMachine;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BankReconciliationServiceImpl reconciliationService;

    private final String validSecret = "kfpcl_bank_webhook_secret_2026";
    private EscrowAccount escrowAccount;
    private PaymentTransaction transaction;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reconciliationService, "configuredWebhookSecret", validSecret);

        escrowAccount = EscrowAccount.builder()
                .id(1L)
                .orderId("ORD-1001")
                .virtualAccountNumber("KFPCLORD1001")
                .ifscCode("KFPCL00001")
                .totalAmountLocked(new BigDecimal("50000.00"))
                .releaseStatus(EscrowReleaseStatus.LOCKED)
                .build();

        transaction = PaymentTransaction.builder()
                .id(10L)
                .transactionReference("TXN-BNK-001")
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .status(PaymentStatus.PAYMENT_PROCESSING)
                .utrNumber("UTR20260829SBI01")
                .build();
    }

    @Test
    void testProcessReconciliation_Success() {
        when(webhookEventRepository.existsByEventId("EVT-BNK-001")).thenReturn(false);
        when(escrowAccountRepository.findByVirtualAccountNumber("KFPCLORD1001")).thenReturn(Optional.of(escrowAccount));
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        Invoice invoice = Invoice.builder()
                .id(5L)
                .orderId("ORD-1001")
                .invoiceNumber("TAX-INV-2026-99")
                .invoiceType(InvoiceType.TAX_INVOICE)
                .build();
        when(invoiceService.generateOrGetInvoice(any(), eq(InvoiceType.TAX_INVOICE))).thenReturn(invoice);

        BankReconciliationWebhookRequest request = BankReconciliationWebhookRequest.builder()
                .eventId("EVT-BNK-001")
                .virtualAccountNumber("KFPCLORD1001")
                .utrNumber("UTR20260829SBI01")
                .amount(new BigDecimal("50000.00"))
                .creditTimestamp(LocalDateTime.now())
                .build();

        BankReconciliationWebhookResponse response = reconciliationService.processReconciliation(
                request, validSecret, "127.0.0.1");

        assertNotNull(response);
        assertEquals("EVT-BNK-001", response.getEventId());
        assertEquals(PaymentStatus.DISPATCH_ALLOWED, response.getPaymentStatus());
        assertTrue(response.getEscrowLocked());
        assertTrue(response.getDispatchAuthorized());
        assertEquals("TAX-INV-2026-99", response.getInvoiceNumber());

        verify(webhookEventRepository).save(any(WebhookEvent.class));
        verify(orderPaymentClient).updateOrderPaymentStatus("ORD-1001", "PAID");
        verify(eventPublisher, times(3)).publishEvent(any(Object.class));
        verify(auditService).logAction(any(), eq("ORD-1001"), eq("BANK_WEBHOOK_RECONCILED"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testProcessReconciliation_InvalidSecretThrowsUnauthorized() {
        BankReconciliationWebhookRequest request = BankReconciliationWebhookRequest.builder()
                .eventId("EVT-BNK-001")
                .virtualAccountNumber("KFPCLORD1001")
                .utrNumber("UTR20260829SBI01")
                .amount(new BigDecimal("50000.00"))
                .creditTimestamp(LocalDateTime.now())
                .build();

        assertThrows(UnauthorizedException.class, () ->
                reconciliationService.processReconciliation(request, "invalid_secret", "127.0.0.1")
        );
    }

    @Test
    void testProcessReconciliation_DuplicateEventThrowsConflict() {
        when(webhookEventRepository.existsByEventId("EVT-DUPLICATE")).thenReturn(true);

        BankReconciliationWebhookRequest request = BankReconciliationWebhookRequest.builder()
                .eventId("EVT-DUPLICATE")
                .virtualAccountNumber("KFPCLORD1001")
                .utrNumber("UTR20260829SBI01")
                .amount(new BigDecimal("50000.00"))
                .creditTimestamp(LocalDateTime.now())
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                reconciliationService.processReconciliation(request, validSecret, "127.0.0.1")
        );
    }

    @Test
    void testProcessReconciliation_AmountMismatchThrowsBadRequest() {
        when(webhookEventRepository.existsByEventId("EVT-BNK-002")).thenReturn(false);
        when(escrowAccountRepository.findByVirtualAccountNumber("KFPCLORD1001")).thenReturn(Optional.of(escrowAccount));
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(transaction));

        BankReconciliationWebhookRequest request = BankReconciliationWebhookRequest.builder()
                .eventId("EVT-BNK-002")
                .virtualAccountNumber("KFPCLORD1001")
                .utrNumber("UTR20260829SBI01")
                .amount(new BigDecimal("35000.00")) // Mismatch! Required: 50000
                .creditTimestamp(LocalDateTime.now())
                .build();

        assertThrows(BadRequestException.class, () ->
                reconciliationService.processReconciliation(request, validSecret, "127.0.0.1")
        );
    }
}
