package com.payment.service;

import com.payment.entity.EscrowAccount;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.DisputeStatus;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.event.EscrowLockedEvent;
import com.payment.event.FundsReleasedEvent;
import com.payment.exception.InvalidPaymentStateException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentDisputeRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.EscrowServiceImpl;
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
class EscrowServiceTest {

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private PaymentDisputeRepository disputeRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private PaymentStateTransitionService stateTransitionService;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EscrowServiceImpl escrowService;

    private OrderDetailsDto orderDetails;
    private EscrowAccount lockedEscrow;
    private PaymentTransaction transaction;

    @BeforeEach
    void setUp() {
        orderDetails = OrderDetailsDto.builder()
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .grandTotal(new BigDecimal("50000.00"))
                .currency("INR")
                .orderStatus("DELIVERED")
                .build();

        lockedEscrow = EscrowAccount.builder()
                .id(1L)
                .orderId("ORD-1001")
                .virtualAccountNumber("KFPCLORD1001")
                .ifscCode("KFPCL00001")
                .totalAmountLocked(new BigDecimal("50000.00"))
                .releaseStatus(EscrowReleaseStatus.LOCKED)
                .lockedAt(LocalDateTime.now())
                .build();

        transaction = PaymentTransaction.builder()
                .id(10L)
                .orderId("ORD-1001")
                .status(PaymentStatus.DISPATCH_ALLOWED)
                .build();
    }

    @Test
    void testReleaseFundsOnDelivery_Success() {
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(lockedEscrow));
        when(disputeRepository.existsByOrderIdAndStatusIn(eq("ORD-1001"), any())).thenReturn(false);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(escrowAccountRepository.save(any(EscrowAccount.class))).thenReturn(lockedEscrow);
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(transaction));

        EscrowAccount result = escrowService.releaseFundsOnDelivery("ORD-1001", "127.0.0.1");

        assertNotNull(result);
        assertEquals(EscrowReleaseStatus.RELEASED_TO_SELLER, result.getReleaseStatus());
        assertNotNull(result.getReleasedAt());

        verify(eventPublisher).publishEvent(any(FundsReleasedEvent.class));
        verify(stateTransitionService).transition(eq(transaction), eq(PaymentStatus.FUNDS_RELEASED), any(), any(), any(), any());
    }

    @Test
    void testReleaseFundsOnDelivery_ActiveDisputeThrowsException() {
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(lockedEscrow));
        when(disputeRepository.existsByOrderIdAndStatusIn(eq("ORD-1001"), any())).thenReturn(true);

        assertThrows(InvalidPaymentStateException.class, () ->
                escrowService.releaseFundsOnDelivery("ORD-1001", "127.0.0.1")
        );
    }

    @Test
    void testRejectReleaseAfterRefund() {
        lockedEscrow.setReleaseStatus(EscrowReleaseStatus.REFUNDED_TO_BUYER);
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(lockedEscrow));

        assertThrows(InvalidPaymentStateException.class, () ->
                escrowService.releaseFundsOnDelivery("ORD-1001", "127.0.0.1")
        );
    }

    @Test
    void testRejectRefundAfterRelease() {
        lockedEscrow.setReleaseStatus(EscrowReleaseStatus.RELEASED_TO_SELLER);
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(lockedEscrow));

        assertThrows(InvalidPaymentStateException.class, () ->
                escrowService.refundEscrow("ORD-1001", "127.0.0.1")
        );
    }
}
