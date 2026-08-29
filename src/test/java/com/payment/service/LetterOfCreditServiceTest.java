package com.payment.service;

import com.payment.dto.lc.LcStatusResponse;
import com.payment.dto.lc.LcUploadRequest;
import com.payment.dto.lc.LcUploadResponse;
import com.payment.dto.lc.LcVerificationRequest;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.LetterOfCredit;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.InvoiceType;
import com.payment.entity.enums.LcStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.exception.BadRequestException;
import com.payment.exception.DuplicateResourceException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.LetterOfCreditRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.LetterOfCreditServiceImpl;
import com.payment.service.state.PaymentStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LetterOfCreditServiceTest {

    @Mock
    private LetterOfCreditRepository letterOfCreditRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentStateMachine stateMachine;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private PaymentAuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LetterOfCreditServiceImpl lcService;

    private UserContext buyerUser;
    private UserContext financeUser;
    private OrderDetailsDto orderDetails;

    @BeforeEach
    void setUp() {
        buyerUser = UserContext.builder()
                .userId("BUYER-101")
                .email("buyer101@kfpcl.com")
                .role(UserRole.BUYER)
                .build();

        financeUser = UserContext.builder()
                .userId("FINANCE-01")
                .email("finance@kfpcl.com")
                .role(UserRole.FINANCE)
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
    void testUploadLetterOfCredit_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(letterOfCreditRepository.existsByLcNumber("LC-2026-SBI-001")).thenReturn(false);
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenAnswer(i -> {
            PaymentTransaction txn = i.getArgument(0);
            txn.setId(10L);
            return txn;
        });
        when(letterOfCreditRepository.save(any(LetterOfCredit.class))).thenAnswer(i -> {
            LetterOfCredit lc = i.getArgument(0);
            lc.setId(1L);
            return lc;
        });

        LcUploadRequest request = LcUploadRequest.builder()
                .orderId("ORD-1001")
                .lcNumber("LC-2026-SBI-001")
                .issuingBank("State Bank of India")
                .advisingBank("HDFC Bank")
                .lcAmount(new BigDecimal("50000.00"))
                .expiryDate(LocalDate.now().plusDays(90))
                .tenorDays(60)
                .documentUrl("https://storage.kfpcl.com/lc/lc-001.pdf")
                .build();

        LcUploadResponse response = lcService.uploadLetterOfCredit(request, null, "127.0.0.1");

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals("LC-2026-SBI-001", response.getLcNumber());
        assertEquals(LcStatus.SUBMITTED, response.getStatus());

        verify(securityUtils).verifyBuyerOwnership("BUYER-101");
        verify(letterOfCreditRepository).save(any(LetterOfCredit.class));
    }

    @Test
    void testUploadLetterOfCredit_DuplicateNumberThrowsConflict() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(letterOfCreditRepository.existsByLcNumber("LC-DUPLICATE")).thenReturn(true);

        LcUploadRequest request = LcUploadRequest.builder()
                .orderId("ORD-1001")
                .lcNumber("LC-DUPLICATE")
                .issuingBank("State Bank of India")
                .advisingBank("HDFC Bank")
                .lcAmount(new BigDecimal("50000.00"))
                .expiryDate(LocalDate.now().plusDays(90))
                .tenorDays(60)
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                lcService.uploadLetterOfCredit(request, null, "127.0.0.1")
        );
    }

    @Test
    void testVerifyLetterOfCredit_ApproveLocksEscrowAndAuthorizesDispatch() {
        when(securityUtils.getCurrentUser()).thenReturn(financeUser);

        LetterOfCredit lc = LetterOfCredit.builder()
                .id(1L)
                .orderId("ORD-1001")
                .lcNumber("LC-2026-SBI-001")
                .lcAmount(new BigDecimal("50000.00"))
                .status(LcStatus.SUBMITTED)
                .build();
        when(letterOfCreditRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(lc));

        PaymentTransaction txn = PaymentTransaction.builder()
                .id(10L)
                .orderId("ORD-1001")
                .buyerId("BUYER-101")
                .amount(new BigDecimal("50000.00"))
                .currency("INR")
                .status(PaymentStatus.PENDING_PAYMENT)
                .build();
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(txn));
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());
        when(escrowAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(letterOfCreditRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Invoice invoice = Invoice.builder().invoiceNumber("TAX-INV-LC-01").build();
        when(invoiceService.generateOrGetInvoice(any(), eq(InvoiceType.TAX_INVOICE))).thenReturn(invoice);

        LcVerificationRequest verifyReq = LcVerificationRequest.builder()
                .approved(true)
                .verificationRemarks("LC verified with SBI issuing branch.")
                .build();

        LcStatusResponse response = lcService.verifyLetterOfCredit("ORD-1001", verifyReq, "127.0.0.1");

        assertNotNull(response);
        assertEquals(LcStatus.APPROVED, response.getStatus());
        assertEquals("FINANCE-01", response.getVerifiedBy());

        verify(securityUtils).requireRole(UserRole.ADMIN, UserRole.FINANCE);
        verify(orderPaymentClient).updateOrderPaymentStatus("ORD-1001", "PAID");
        verify(eventPublisher, times(3)).publishEvent(any(Object.class));
    }
}
