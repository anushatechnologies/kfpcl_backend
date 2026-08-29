package com.payment.controller;

import com.payment.dto.invoice.SendInvoiceEmailResponse;
import com.payment.entity.enums.InvoiceType;
import com.payment.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Invoicing & Documents", description = "Endpoints for GST Tax / Proforma Invoice Generation, PDF Downloads, and Email Dispatch")
public class OrderInvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "10. Download GST Proforma/Tax Invoice PDF (:orderId)",
            description = "Generates and streams a GST-compliant PDF invoice with GSTIN, HSN, CGST, SGST, IGST breakdown, and escrow payment instructions.")
    @GetMapping(value = "/{orderId}/proforma-invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProformaInvoicePdf(
            @Parameter(description = "Order ID in :orderId notation") @PathVariable String orderId,
            @Parameter(description = "Invoice type (PROFORMA or TAX_INVOICE)")
            @RequestParam(defaultValue = "PROFORMA") InvoiceType type) {

        byte[] pdfBytes = invoiceService.generateInvoicePdf(orderId, type);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice-" + orderId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @Operation(summary = "11. Send Invoice Email (:orderId)",
            description = "Sends the GST invoice email with PDF attachment to the order buyer/participants. Async delivery support.")
    @PostMapping("/{orderId}/send-invoice-email")
    public ResponseEntity<SendInvoiceEmailResponse> sendInvoiceEmail(
            @Parameter(description = "Order ID in :orderId notation") @PathVariable String orderId) {

        SendInvoiceEmailResponse response = invoiceService.sendInvoiceEmail(orderId);
        return ResponseEntity.ok(response);
    }
}
