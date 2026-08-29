package com.payment.service;

import com.payment.dto.gateway.VerifyPaymentRequest;
import com.payment.dto.gateway.VerifyPaymentResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.*;
import com.payment.exception.BadRequestException;
import com.payment.gateway.PaymentGateway;
import com.payment.gateway.PaymentGatewayFactory;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.PaymentVerificationServiceImpl;
import com.payment.service.state.PaymentStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentVerificationServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private PaymentGatewayFactory gatewayFactory;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentStateMachine stateMachine;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentVerificationServiceImpl verificationService;

    private UserContext buyerUser;
    private PaymentTransaction pendingTxn;

    @BeforeEach
    void setUp() {
        buyerUser = UserContext.builder()
                .userId("BUYER-101")
                .email("buyer101@kfpcl.com")
                .role(UserRole.BUYER)
                .build();

        pendingTxn = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("TXN-REF-101")
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .gateway(PaymentGatewayType.RAZORPAY)
                .paymentMethod(PaymentMethod.CARD)
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .status(PaymentStatus.PENDING_PAYMENT)
                .gatewayOrderId("order_rzp_123")
                .recipientEmail("buyer101@kfpcl.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testVerifyPayment_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(pendingTxn));
        when(gatewayFactory.getGateway(PaymentGatewayType.RAZORPAY)).thenReturn(paymentGateway);
        when(paymentGateway.verifySignature("order_rzp_123", "pay_rzp_456", "valid_signature")).thenReturn(true);
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());
        when(escrowAccountRepository.save(any(EscrowAccount.class))).thenAnswer(i -> {
            EscrowAccount ea = i.getArgument(0);
            ea.setId(100L);
            return ea;
        });

        Invoice taxInvoice = Invoice.builder()
                .id(50L)
                .orderId("ORD-1001")
                .invoiceNumber("TAX-INV-2026-001")
                .invoiceType(InvoiceType.TAX_INVOICE)
                .generatedAt(LocalDateTime.now())
                .build();
        when(invoiceService.generateOrGetInvoice(any(), eq(InvoiceType.TAX_INVOICE))).thenReturn(taxInvoice);

        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .orderId("ORD-1001")
                .gatewayOrderId("order_rzp_123")
                .paymentId("pay_rzp_456")
                .signature("valid_signature")
                .build();

        VerifyPaymentResponse response = verificationService.verifyPayment(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals(PaymentStatus.DISPATCH_ALLOWED, response.getPaymentStatus());
        assertTrue(response.getEscrowLocked());
        assertEquals(EscrowReleaseStatus.LOCKED, response.getEscrowStatus());
        assertEquals("TAX-INV-2026-001", response.getInvoiceNumber());
        assertTrue(response.getDispatchAllowed());

        verify(securityUtils).verifyBuyerOwnership("BUYER-101");
        verify(orderPaymentClient).updateOrderPaymentStatus("ORD-1001", "PAID");
        verify(eventPublisher, times(3)).publishEvent(any(Object.class));
        verify(auditService).logAction(any(), eq("ORD-1001"), eq("VERIFY_PAYMENT_AND_LOCK_ESCROW"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testVerifyPayment_InvalidSignatureThrowsBadRequest() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(pendingTxn));
        when(gatewayFactory.getGateway(PaymentGatewayType.RAZORPAY)).thenReturn(paymentGateway);
        when(paymentGateway.verifySignature("order_rzp_123", "pay_rzp_456", "invalid_sig")).thenReturn(false);

        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .orderId("ORD-1001")
                .gatewayOrderId("order_rzp_123")
                .paymentId("pay_rzp_456")
                .signature("invalid_sig")
                .build();

        assertThrows(BadRequestException.class, () ->
                verificationService.verifyPayment(request, "127.0.0.1")
        );
    }
}
