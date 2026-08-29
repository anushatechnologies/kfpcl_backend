package com.payment.service;

import com.payment.dto.escrow.VirtualAccountResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.exception.ForbiddenException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.impl.VirtualAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VirtualAccountServiceTest {

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private VirtualAccountServiceImpl virtualAccountService;

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
                .orderStatus("CREATED")
                .paymentStatus("UNPAID")
                .customerEmail("buyer101@kfpcl.com")
                .build();
    }

    @Test
    void testGetVirtualAccountDetails_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);

        EscrowAccount existingEscrow = EscrowAccount.builder()
                .id(1L)
                .orderId("ORD-1001")
                .virtualAccountNumber("KFPCLORD1001")
                .ifscCode("KFPCL00001")
                .totalAmountLocked(new BigDecimal("50000.00"))
                .releaseStatus(EscrowReleaseStatus.LOCKED)
                .build();

        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(existingEscrow));
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());

        VirtualAccountResponse response = virtualAccountService.getVirtualAccountDetails("ORD-1001");

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals("KFPCLORD1001", response.getVirtualAccountNumber());
        assertEquals("KFPCL00001", response.getIfscCode());
        assertEquals(new BigDecimal("50000.00"), response.getAmount());
        assertEquals(PaymentStatus.PENDING_PAYMENT, response.getPaymentStatus());

        verify(securityUtils).verifyBuyerOwnership("BUYER-101");
        // Verify order is NOT marked as paid
        verify(orderPaymentClient, never()).updateOrderPaymentStatus(any(), any());
    }

    @Test
    void testGetVirtualAccountDetails_UnauthorizedThrowsForbidden() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        doThrow(new ForbiddenException("Access denied"))
                .when(securityUtils).verifyBuyerOwnership("BUYER-101");

        assertThrows(ForbiddenException.class, () ->
                virtualAccountService.getVirtualAccountDetails("ORD-1001")
        );
    }
}
