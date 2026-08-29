package com.payment.service;

import com.payment.dto.invoice.SendInvoiceEmailResponse;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.InvoiceType;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.InvoiceRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.impl.InvoiceServiceImpl;
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
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private EscrowAccountRepository escrowAccountRepository;

    @Mock
    private OrderPaymentClient orderPaymentClient;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PaymentAuditService auditService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

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
                .paymentStatus("PAID")
                .customerEmail("buyer101@kfpcl.com")
                .build();
    }

    @Test
    void testGenerateInvoicePdf_ReturnsPdfBytes() {
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());
        when(escrowAccountRepository.findByOrderId("ORD-1001")).thenReturn(Optional.empty());

        byte[] pdfBytes = invoiceService.generateInvoicePdf("ORD-1001", InvoiceType.PROFORMA);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verify PDF Magic Bytes (%PDF-)
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);

        verify(securityUtils).verifyOrderParticipant("BUYER-101", "SELLER-501");
    }

    @Test
    void testSendInvoiceEmail_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(buyerUser);
        when(orderPaymentClient.getOrderDetails("ORD-1001")).thenReturn(orderDetails);

        PaymentTransaction txn = PaymentTransaction.builder()
                .id(1L)
                .orderId("ORD-1001")
                .recipientEmail("buyer101@kfpcl.com")
                .status(PaymentStatus.DISPATCH_ALLOWED)
                .build();
        when(transactionRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(txn));

        Invoice invoice = Invoice.builder()
                .id(2L)
                .orderId("ORD-1001")
                .invoiceNumber("TAX-INV-2026-001")
                .recipientEmail("buyer101@kfpcl.com")
                .invoiceType(InvoiceType.TAX_INVOICE)
                .generatedAt(LocalDateTime.now())
                .build();
        when(invoiceRepository.findByOrderIdAndInvoiceType("ORD-1001", InvoiceType.TAX_INVOICE))
                .thenReturn(Optional.of(invoice));

        SendInvoiceEmailResponse response = invoiceService.sendInvoiceEmail("ORD-1001");

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderId());
        assertEquals("TAX-INV-2026-001", response.getInvoiceNumber());
        assertTrue(response.getEmailSent());
        assertEquals("buyer101@kfpcl.com", response.getRecipientEmail());

        verify(invoiceRepository).save(any(Invoice.class));
        verify(transactionRepository).save(any(PaymentTransaction.class));
        verify(auditService).logAction(any(), eq("ORD-1001"), eq("INVOICE_EMAIL_SENT"), any(), any(), any(), any(), any(), any());
    }
}
