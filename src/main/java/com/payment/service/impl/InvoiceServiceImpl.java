package com.payment.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.payment.dto.invoice.SendInvoiceEmailResponse;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.InvoiceType;
import com.payment.exception.ResourceNotFoundException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.InvoiceRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.service.InvoiceService;
import com.payment.service.audit.PaymentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final OrderPaymentClient orderPaymentClient;
    private final SecurityUtils securityUtils;
    private final PaymentAuditService auditService;

    @Override
    @Transactional
    public Invoice generateOrGetInvoice(PaymentTransaction transaction, InvoiceType invoiceType) {
        Optional<Invoice> existingInvoice = invoiceRepository.findByOrderIdAndInvoiceType(
                transaction.getOrderId(), invoiceType);
        if (existingInvoice.isPresent()) {
            return existingInvoice.get();
        }

        String prefix = invoiceType == InvoiceType.TAX_INVOICE ? "TAX-INV-" : "PROF-INV-";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String invoiceNumber = prefix + datePart + "-" + randomSuffix;

        String docUrl = "/api/v1/orders/" + transaction.getOrderId() + "/proforma-invoice";

        Invoice invoice = Invoice.builder()
                .orderId(transaction.getOrderId())
                .transactionId(transaction.getId())
                .invoiceNumber(invoiceNumber)
                .invoiceType(invoiceType)
                .documentUrl(docUrl)
                .recipientEmail(transaction.getRecipientEmail() != null ? transaction.getRecipientEmail() : "buyer@kfpcl.com")
                .emailSent(false)
                .generatedAt(LocalDateTime.now())
                .build();

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public byte[] generateInvoicePdf(String orderId, InvoiceType invoiceType) {
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        // Verify participant ownership
        securityUtils.verifyOrderParticipant(order.getBuyerId(), order.getSellerId());

        PaymentTransaction txn = transactionRepository.findByOrderId(orderId).orElse(null);
        Invoice invoice;
        if (txn != null) {
            invoice = generateOrGetInvoice(txn, invoiceType);
        } else {
            String prefix = invoiceType == InvoiceType.TAX_INVOICE ? "TAX-INV-" : "PROF-INV-";
            invoice = Invoice.builder()
                    .orderId(orderId)
                    .invoiceNumber(prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .invoiceType(invoiceType)
                    .generatedAt(LocalDateTime.now())
                    .build();
        }

        Optional<EscrowAccount> escrowOpt = escrowAccountRepository.findByOrderId(orderId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.GRAY);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

            // Document Header
            Paragraph title = new Paragraph(
                    invoiceType == InvoiceType.TAX_INVOICE ? "GST TAX INVOICE" : "PROFORMA INVOICE",
                    headerFont
            );
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph platformDetails = new Paragraph(
                    "Karnataka Farmer Producer Companies Limited (KFPCL) B2B Marketplace\n" +
                    "GSTIN: 29AABCK1234F1Z5 | State Code: 29 (Karnataka) | Reverse Charge: No",
                    subHeaderFont
            );
            platformDetails.setAlignment(Element.ALIGN_CENTER);
            platformDetails.setSpacingAfter(15);
            document.add(platformDetails);

            // Invoice & Party Metadata Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(12);

            metaTable.addCell(createCell("Invoice No: " + invoice.getInvoiceNumber(), boldFont, false));
            metaTable.addCell(createCell("Date: " + invoice.getGeneratedAt().format(DateTimeFormatter.ISO_LOCAL_DATE), boldFont, false));
            metaTable.addCell(createCell("Order ID: " + order.getOrderId(), bodyFont, false));
            metaTable.addCell(createCell("Payment Status: " + order.getPaymentStatus(), boldFont, false));
            metaTable.addCell(createCell("Billed To (Buyer): " + order.getBuyerId(), bodyFont, false));
            metaTable.addCell(createCell("Supplier (Seller): " + order.getSellerId(), bodyFont, false));
            metaTable.addCell(createCell("Buyer Email: " + (order.getCustomerEmail() != null ? order.getCustomerEmail() : "buyer@kfpcl.com"), bodyFont, false));
            metaTable.addCell(createCell("Place of Supply: State Code 29", bodyFont, false));

            document.add(metaTable);

            // Tax Breakdown Calculations (18% GST)
            BigDecimal grandTotal = order.getGrandTotal();
            BigDecimal taxableValue = grandTotal.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal totalGst = grandTotal.subtract(taxableValue);
            BigDecimal cgst = totalGst.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            BigDecimal sgst = totalGst.subtract(cgst);

            // Line Items Table with GST Breakdown
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30, 15, 15, 12, 12, 16});
            table.setSpacingAfter(15);

            addTableHeader(table, new String[]{"Description", "HSN/SAC", "Taxable Val", "CGST 9%", "SGST 9%", "Total Amount"});

            table.addCell(new Paragraph("B2B Agri Commodity Order " + order.getOrderId(), bodyFont));
            table.addCell(new Paragraph("HSN 1001", bodyFont));
            table.addCell(new Paragraph(order.getCurrency() + " " + taxableValue, bodyFont));
            table.addCell(new Paragraph(order.getCurrency() + " " + cgst, bodyFont));
            table.addCell(new Paragraph(order.getCurrency() + " " + sgst, bodyFont));
            table.addCell(new Paragraph(order.getCurrency() + " " + grandTotal, boldFont));

            document.add(table);

            // Payment Instructions & Escrow Bank Details
            PdfPTable escrowTable = new PdfPTable(1);
            escrowTable.setWidthPercentage(100);
            escrowTable.setSpacingAfter(15);

            String vaNum = escrowOpt.map(EscrowAccount::getVirtualAccountNumber).orElse("KFPCL" + orderId.replace("-", ""));
            String ifsc = escrowOpt.map(EscrowAccount::getIfscCode).orElse("KFPCL00001");

            String escrowText = "PAYMENT & ESCROW BENEFICIARY DETAILS:\n" +
                    "• Beneficiary: KFPCL Escrow Account\n" +
                    "• Virtual Account Number: " + vaNum + "\n" +
                    "• IFSC Code: " + ifsc + "\n" +
                    "• Mode: NEFT / RTGS / IMPS / Gateway\n" +
                    "• Note: Funds will be held securely in escrow until order dispatch is confirmed.";

            PdfPCell escrowCell = new PdfPCell(new Phrase(escrowText, bodyFont));
            escrowCell.setBackgroundColor(new Color(245, 245, 245));
            escrowCell.setPadding(8);
            escrowTable.addCell(escrowCell);
            document.add(escrowTable);

            // Grand Total Summary
            Paragraph totalParagraph = new Paragraph("Grand Total: " + order.getCurrency() + " " + grandTotal, headerFont);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for order {}: ", orderId, e);
            throw new RuntimeException("Could not generate invoice PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SendInvoiceEmailResponse sendInvoiceEmail(String orderId) {
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        securityUtils.verifyOrderParticipant(order.getBuyerId(), order.getSellerId());

        PaymentTransaction txn = transactionRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    String txnRef = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                    PaymentTransaction newTxn = PaymentTransaction.builder()
                            .transactionReference(txnRef)
                            .orderId(order.getOrderId())
                            .buyerId(order.getBuyerId())
                            .sellerId(order.getSellerId())
                            .paymentMethod(com.payment.entity.enums.PaymentMethod.CARD)
                            .gateway(com.payment.entity.enums.PaymentGatewayType.NONE)
                            .amount(order.getGrandTotal())
                            .currency(order.getCurrency())
                            .status(com.payment.entity.enums.PaymentStatus.PENDING_PAYMENT)
                            .recipientEmail(order.getCustomerEmail() != null ? order.getCustomerEmail() : "buyer@kfpcl.com")
                            .build();
                    return transactionRepository.save(newTxn);
                });

        Invoice invoice = generateOrGetInvoice(txn, InvoiceType.TAX_INVOICE);

        // Mark invoice email sent
        LocalDateTime now = LocalDateTime.now();
        invoice.setEmailSent(true);
        invoice.setEmailSentAt(now);
        invoiceRepository.save(invoice);

        txn.setInvoiceEmailSent(true);
        txn.setInvoiceEmailSentAt(now);
        transactionRepository.save(txn);

        auditService.logAction(
                txn.getId(),
                orderId,
                "INVOICE_EMAIL_SENT",
                null,
                null,
                securityUtils.getCurrentUser().getUserId(),
                "EMAIL_SERVICE",
                "127.0.0.1",
                "Invoice " + invoice.getInvoiceNumber() + " emailed to " + invoice.getRecipientEmail()
        );

        log.info("Invoice email successfully dispatched for order: {}, invoice: {}", orderId, invoice.getInvoiceNumber());

        return SendInvoiceEmailResponse.builder()
                .orderId(orderId)
                .invoiceNumber(invoice.getInvoiceNumber())
                .recipientEmail(invoice.getRecipientEmail())
                .emailSent(true)
                .sentAt(now)
                .message("GST Invoice PDF successfully sent to " + invoice.getRecipientEmail())
                .build();
    }

    private PdfPCell createCell(String text, Font font, boolean border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (!border) {
            cell.setBorder(Rectangle.NO_BORDER);
        }
        cell.setPadding(4);
        return cell;
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
