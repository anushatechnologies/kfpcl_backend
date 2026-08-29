package com.payment.service;

import com.payment.dto.dispute.DisputeResponse;
import com.payment.dto.dispute.RaiseDisputeRequest;
import com.payment.entity.PaymentDispute;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.DisputeStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.event.PaymentDisputedEvent;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.PaymentDisputeRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.PaymentDisputeServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentDisputeServiceTest {

    @Mock
    private PaymentDisputeRepository disputeRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private PaymentStateMachine stateMachine;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentDisputeServiceImpl disputeService;

    private UserContext buyerUser;
    private OrderDetailsDto orderDetails;

    @BeforeEach
    void setUp() {
        buyerUser = UserContext.builder()
                .userId("BUYER-101")
                .email("buyer101@kfpcl.com")
                .role(UserRole.BUYER)
                .build();

        orderDetails = OrderDetailsDto.builder()
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .grandTotal(new BigDecimal("50000.00"))
                .currency("INR")
                .orderStatus("CONFIRMED")
                .paymentStatus("PAID")
                .build();
    }

    @Test
    void testRaiseDispute_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(disputeRepository.existsByOrderIdAndStatusIn(eq("ORD-1001"), any())).thenReturn(false);

        PaymentTransaction txn = PaymentTransaction.builder()
                .id(10L)
                .orderId("ORD-1001")
                .status(PaymentStatus.DISPATCH_ALLOWED)
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .build();
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(txn));

        PaymentDispute savedDispute = PaymentDispute.builder()
                .id(1L)
                .orderId("ORD-1001")
                .transactionId(10L)
                .raisedByUserId("BUYER-101")
                .raisedByRole(UserRole.BUYER)
                .reason("Goods received are severely damaged and sub-standard quality")
                .status(DisputeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
        when(disputeRepository.save(any(PaymentDispute.class))).thenReturn(savedDispute);

        RaiseDisputeRequest request = RaiseDisputeRequest.builder()
                .orderId("ORD-1001")
                .reason("Goods received are severely damaged and sub-standard quality")
                .evidenceUrls(List.of("https://storage.kfpcl.com/evidence/damaged-batch.jpg"))
                .build();

        DisputeResponse response = disputeService.raiseDispute(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals(1L, response.getDisputeId());
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals(DisputeStatus.OPEN, response.getDisputeStatus());
        assertEquals(PaymentStatus.DISPUTED, response.getPaymentStatus());
        assertEquals("BUYER-101", response.getRaisedByUserId());

        verify(stateMachine).validateTransition(PaymentStatus.DISPATCH_ALLOWED, PaymentStatus.DISPUTED);
        verify(transactionRepository).save(txn);
        verify(eventPublisher).publishEvent(any(PaymentDisputedEvent.class));
        verify(auditService).logAction(eq(10L), eq("ORD-1001"), eq("PAYMENT_DISPUTED"), any(), eq("DISPUTED"), eq("BUYER-101"), any(), any(), any());
    }
}
