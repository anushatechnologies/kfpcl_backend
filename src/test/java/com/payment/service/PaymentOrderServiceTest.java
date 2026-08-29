package com.payment.service;

import com.payment.dto.gateway.CreateGatewayOrderRequest;
import com.payment.dto.gateway.CreateGatewayOrderResponse;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.exception.ForbiddenException;
import com.payment.gateway.GatewayOrderRequest;
import com.payment.gateway.GatewayOrderResponse;
import com.payment.gateway.PaymentGateway;
import com.payment.gateway.PaymentGatewayFactory;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.PaymentOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private PaymentGatewayFactory gatewayFactory;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentAuditService auditService;

    @InjectMocks
    private PaymentOrderServiceImpl paymentOrderService;

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
    void testCreateGatewayOrder_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(gatewayFactory.getGateway(PaymentGatewayType.RAZORPAY)).thenReturn(paymentGateway);
        when(paymentGateway.createOrder(any(GatewayOrderRequest.class))).thenReturn(
                GatewayOrderResponse.builder()
                        .gatewayOrderId("order_rzp_123456")
                        .amount(new BigDecimal("50000.00"))
                        .currency("INR")
                        .status("created")
                        .build()
        );

        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction txn = invocation.getArgument(0);
            txn.setId(1L);
            txn.setCreatedAt(LocalDateTime.now());
            return txn;
        });

        CreateGatewayOrderRequest request = CreateGatewayOrderRequest.builder()
                .orderId("ORD-1001")
                .gateway(PaymentGatewayType.RAZORPAY)
                .paymentMethod(PaymentMethod.CARD)
                .build();

        CreateGatewayOrderResponse response = paymentOrderService.createGatewayOrder(request, "IDEMP-KEY-001", "127.0.0.1");

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals("order_rzp_123456", response.getGatewayOrderId());
        assertEquals(new BigDecimal("50000.00"), response.getAmount());
        assertEquals(PaymentStatus.PENDING_PAYMENT, response.getStatus());

        verify(securityUtils).verifyBuyerOwnership("BUYER-101");
        verify(transactionRepository).save(any(PaymentTransaction.class));
        verify(auditService).logAction(any(), eq("ORD-1001"), eq("CREATE_GATEWAY_ORDER"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCreateGatewayOrder_IdempotencyReturnsExisting() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);

        PaymentTransaction existingTxn = PaymentTransaction.builder()
                .id(10L)
                .transactionReference("TXN-EXISTING-01")
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .gatewayOrderId("order_rzp_existing")
                .gateway(PaymentGatewayType.RAZORPAY)
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .status(PaymentStatus.PENDING_PAYMENT)
                .idempotencyKey("IDEMP-KEY-001")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByIdempotencyKey("IDEMP-KEY-001")).thenReturn(Optional.of(existingTxn));

        CreateGatewayOrderRequest request = CreateGatewayOrderRequest.builder()
                .orderId("ORD-1001")
                .gateway(PaymentGatewayType.RAZORPAY)
                .build();

        CreateGatewayOrderResponse response = paymentOrderService.createGatewayOrder(request, "IDEMP-KEY-001", "127.0.0.1");

        assertNotNull(response);
        assertEquals("TXN-EXISTING-01", response.getTransactionReference());
        assertEquals("order_rzp_existing", response.getGatewayOrderId());
        verify(transactionRepository, never()).save(any());
        verify(orderPaymentClient, never()).getOrderDetails(any());
    }

    @Test
    void testCreateGatewayOrder_UnauthorizedBuyerThrowsForbidden() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        doThrow(new ForbiddenException("Access denied: you do not have permission"))
                .when(securityUtils).verifyBuyerOwnership("BUYER-101");

        CreateGatewayOrderRequest request = CreateGatewayOrderRequest.builder()
                .orderId("ORD-1001")
                .gateway(PaymentGatewayType.RAZORPAY)
                .build();

        assertThrows(ForbiddenException.class, () ->
                paymentOrderService.createGatewayOrder(request, null, "127.0.0.1")
        );
    }
}
