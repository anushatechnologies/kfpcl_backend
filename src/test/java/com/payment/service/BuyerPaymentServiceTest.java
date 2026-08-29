package com.payment.service;

import com.payment.dto.history.PaymentHistoryResponse;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.repository.InvoiceRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.impl.BuyerPaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyerPaymentServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private BuyerPaymentServiceImpl buyerPaymentService;

    private UserContext buyerUser;

    @BeforeEach
    void setUp() {
        buyerUser = UserContext.builder()
                .userId("BUYER-101")
                .email("buyer101@kfpcl.com")
                .role(UserRole.BUYER)
                .build();
    }

    @Test
    void testGetBuyerPaymentHistory_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);

        PaymentTransaction txn = PaymentTransaction.builder()
                .id(1L)
                .transactionReference("TXN-001")
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .sellerId("SELLER-501")
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .status(PaymentStatus.DISPATCH_ALLOWED)
                .paymentMethod(PaymentMethod.CARD)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findBuyerTransactions(
                eq("BUYER-101"), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(txn)));
        when(invoiceRepository.findByOrderId("ORD-1001")).thenReturn(Collections.emptyList());

        PaymentHistoryResponse response = buyerPaymentService.getBuyerPaymentHistory(
                null, null, null, null, 0, 10, "createdAt", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("ORD-1001", response.getContent().get(0).getOrderId());
        assertEquals("TXN-001", response.getContent().get(0).getTransactionReference());
        assertEquals(1, response.getTotalElements());

        verify(securityUtils).getCurrentUser();
    }
}
