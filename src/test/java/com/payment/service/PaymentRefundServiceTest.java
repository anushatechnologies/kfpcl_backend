package com.payment.service;

import com.payment.dto.refund.ExecuteRefundRequest;
import com.payment.dto.refund.RefundResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.PaymentDispute;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.Refund;
import com.payment.entity.enums.*;
import com.payment.event.RefundCompletedEvent;
import com.payment.gateway.GatewayRefundResponse;
import com.payment.gateway.PaymentGateway;
import com.payment.gateway.PaymentGatewayFactory;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentDisputeRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.repository.RefundRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.PaymentRefundServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private PaymentDisputeRepository disputeRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private PaymentGatewayFactory gatewayFactory;

    @Mock
    private PaymentStateMachine stateMachine;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentGateway razorpayGateway;

    @InjectMocks
    private PaymentRefundServiceImpl refundService;

    private UserContext financeUser;
    private OrderDetailsDto orderDetails;

    @BeforeEach
    void setUp() {
        financeUser = UserContext.builder()
                .userId("FINANCE-001")
                .email("finance001@kfpcl.com")
                .role(UserRole.FINANCE)
                .build();

        orderDetails = OrderDetailsDto.builder()
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .grandTotal(new BigDecimal("50000.00"))
                .currency("INR")
                .orderStatus("DISPUTED")
                .paymentStatus("PAID")
                .build();
    }

    @Test
    void testProcessRefund_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(financeUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);

        PaymentTransaction txn = PaymentTransaction.builder()
                .id(1L)
                .orderId("ORD-1001")
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .status(PaymentStatus.DISPUTED)
                .gateway(PaymentGatewayType.RAZORPAY)
                .gatewayPaymentId("pay_123456")
                .build();
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(txn));

        when(gatewayFactory.getGateway(PaymentGatewayType.RAZORPAY)).thenReturn(razorpayGateway);
        when(razorpayGateway.processRefund(any())).thenReturn(GatewayRefundResponse.builder()
                .gatewayRefundId("rfnd_rzp_999")
                .status("processed")
                .build());

        EscrowAccount escrow = EscrowAccount.builder()
                .id(2L)
                .orderId("ORD-1001")
                .releaseStatus(EscrowReleaseStatus.LOCKED)
                .build();
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(escrow));

        PaymentDispute dispute = PaymentDispute.builder()
                .id(5L)
                .orderId("ORD-1001")
                .status(DisputeStatus.OPEN)
                .build();
        when(disputeRepository.findByOrderIdAndStatusIn(eq("ORD-1001"), any())).thenReturn(Optional.of(dispute));

        Refund savedRefund = Refund.builder()
                .id(100L)
                .orderId("ORD-1001")
                .refundReference("RFND-TEST-01")
                .requestedAmount(new BigDecimal("50000.00"))
                .approvedAmount(new BigDecimal("50000.00"))
                .reason("Quality defect confirmed by resolution team")
                .status(RefundStatus.COMPLETED)
                .gatewayRefundId("rfnd_rzp_999")
                .approvedBy("FINANCE-001")
                .completedAt(LocalDateTime.now())
                .build();
        when(refundRepository.save(any(Refund.class))).thenReturn(savedRefund);

        ExecuteRefundRequest request = ExecuteRefundRequest.builder()
                .orderId("ORD-1001")
                .amount(new BigDecimal("50000.00"))
                .reason("Quality defect confirmed by resolution team")
                .build();

        RefundResponse response = refundService.processRefund(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals(100L, response.getRefundId());
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals(RefundStatus.COMPLETED, response.getRefundStatus());
        assertEquals(PaymentStatus.REFUNDED, response.getPaymentStatus());
        assertEquals(EscrowReleaseStatus.REFUNDED_TO_BUYER, response.getEscrowReleaseStatus());
        assertEquals("rfnd_rzp_999", response.getGatewayRefundId());

        verify(stateMachine).validateTransition(PaymentStatus.DISPUTED, PaymentStatus.REFUNDED);
        verify(transactionRepository).save(txn);
        verify(escrowAccountRepository).save(escrow);
        verify(disputeRepository).save(dispute);
        verify(orderPaymentClient).updateOrderPaymentStatus("ORD-1001", "REFUNDED");
        verify(eventPublisher).publishEvent(any(RefundCompletedEvent.class));
        verify(auditService).logAction(eq(1L), eq("ORD-1001"), eq("PAYMENT_REFUNDED"), eq("DISPUTED"), eq("REFUNDED"), eq("FINANCE-001"), any(), any(), any());
    }
}
