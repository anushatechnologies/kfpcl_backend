package com.payment.service;

import com.payment.dto.bank.BankTransferConfirmRequest;
import com.payment.dto.bank.BankTransferConfirmResponse;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.exception.BadRequestException;
import com.payment.exception.DuplicateResourceException;
import com.payment.exception.ForbiddenException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.BankTransferServiceImpl;
import com.payment.service.state.PaymentStateMachine;
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
class BankTransferServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentStateMachine stateMachine;

    @Mock
    private PaymentAuditService auditService;

    @InjectMocks
    private BankTransferServiceImpl bankTransferService;

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
    void testConfirmBankTransfer_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(transactionRepository.existsByUtrNumber("UTR123456789")).thenReturn(false);
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> {
            PaymentTransaction txn = i.getArgument(0);
            txn.setId(10L);
            return txn;
        });

        BankTransferConfirmRequest request = BankTransferConfirmRequest.builder()
                .orderId("ORD-1001")
                .utrNumber("UTR123456789")
                .remitterBank("State Bank of India")
                .transferDate(LocalDateTime.now())
                .amount(new BigDecimal("50000.00"))
                .receiptDocUrl("https://storage.kfpcl.com/receipts/rec-001.pdf")
                .build();

        BankTransferConfirmResponse response = bankTransferService.confirmBankTransfer(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals("UTR123456789", response.getUtrNumber());
        assertEquals(PaymentStatus.PAYMENT_PROCESSING, response.getPaymentStatus());
        assertFalse(response.getEscrowLocked()); // Must NOT lock escrow yet

        verify(securityUtils).verifyBuyerOwnership("BUYER-101");
        verify(transactionRepository).save(any(PaymentTransaction.class));
        verify(auditService).logAction(any(), eq("ORD-1001"), eq("SUBMIT_BANK_TRANSFER_UTR"), any(), eq("PAYMENT_PROCESSING"), any(), any(), any(), any());
    }

    @Test
    void testConfirmBankTransfer_DuplicateUtrThrowsConflict() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(transactionRepository.existsByUtrNumber("UTR123456789")).thenReturn(true);

        BankTransferConfirmRequest request = BankTransferConfirmRequest.builder()
                .orderId("ORD-1001")
                .utrNumber("UTR123456789")
                .remitterBank("HDFC Bank")
                .transferDate(LocalDateTime.now())
                .amount(new BigDecimal("50000.00"))
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                bankTransferService.confirmBankTransfer(request, "127.0.0.1")
        );
    }

    @Test
    void testConfirmBankTransfer_AmountMismatchThrowsBadRequest() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(transactionRepository.existsByUtrNumber("UTR-NEW")).thenReturn(false);

        BankTransferConfirmRequest request = BankTransferConfirmRequest.builder()
                .orderId("ORD-1001")
                .utrNumber("UTR-NEW")
                .remitterBank("ICICI Bank")
                .transferDate(LocalDateTime.now())
                .amount(new BigDecimal("40000.00")) // Mismatch! Required: 50000
                .build();

        assertThrows(BadRequestException.class, () ->
                bankTransferService.confirmBankTransfer(request, "127.0.0.1")
        );
    }
}
