package com.payment.controller;

import com.payment.dto.bank.BankTransferConfirmRequest;
import com.payment.dto.bank.BankTransferConfirmResponse;
import com.payment.dto.escrow.VirtualAccountResponse;
import com.payment.dto.gateway.CreateGatewayOrderRequest;
import com.payment.dto.gateway.CreateGatewayOrderResponse;
import com.payment.dto.gateway.VerifyPaymentRequest;
import com.payment.dto.gateway.VerifyPaymentResponse;
import com.payment.dto.lc.LcStatusResponse;
import com.payment.dto.lc.LcUploadRequest;
import com.payment.dto.lc.LcUploadResponse;
import com.payment.dto.lc.LcVerificationRequest;
import com.payment.dto.webhook.BankReconciliationWebhookRequest;
import com.payment.dto.webhook.BankReconciliationWebhookResponse;
import com.payment.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for Payment initialization, verification, virtual accounts, bank transfers, LC, payouts, and disputes")
public class PaymentController {

    private final PaymentOrderService paymentOrderService;
    private final PaymentVerificationService paymentVerificationService;
    private final VirtualAccountService virtualAccountService;
    private final BankTransferService bankTransferService;
    private final BankReconciliationService bankReconciliationService;
    private final LetterOfCreditService letterOfCreditService;
    private final com.payment.service.PaymentDisputeService disputeService;
    private final com.payment.service.PaymentRefundService refundService;

    @Operation(summary = "1. Create Gateway Order", description = "Initializes a Razorpay, Cashfree, or Stripe gateway order for an order. Protected buyer.")
    @PostMapping("/create-gateway-order")
    public ResponseEntity<CreateGatewayOrderResponse> createGatewayOrder(
            @Valid @RequestBody CreateGatewayOrderRequest request,
            @Parameter(description = "Optional idempotency key for replay-safe execution")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        CreateGatewayOrderResponse response = paymentOrderService.createGatewayOrder(request, idempotencyKey, clientIp);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "2. Verify Payment & Lock Escrow", description = "Verifies gateway payment signature, locks escrow, authorizes dispatch, and triggers invoice generation. Protected buyer.")
    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        VerifyPaymentResponse response = paymentVerificationService.verifyPayment(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "3. Fetch Escrow Virtual Account Details (:orderId)", description = "Fetches escrow virtual bank account details for bank transfer/NEFT/RTGS. Protected buyer.")
    @GetMapping("/virtual-account/{orderId}")
    public ResponseEntity<VirtualAccountResponse> getVirtualAccountDetails(
            @Parameter(description = "Order ID in :orderId notation") @PathVariable String orderId) {

        VirtualAccountResponse response = virtualAccountService.getVirtualAccountDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "4. Submit Bank Transfer / UTR Confirmation", description = "Submits bank UTR and receipt details. Updates status to PAYMENT_PROCESSING. Protected buyer.")
    @PostMapping("/bank-transfer/confirm")
    public ResponseEntity<BankTransferConfirmResponse> confirmBankTransfer(
            @Valid @RequestBody BankTransferConfirmRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        BankTransferConfirmResponse response = bankTransferService.confirmBankTransfer(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "5. Automated Bank Reconciliation Webhook", description = "Webhook for bank credit notification. Validates webhook secret, deduplicates event, locks escrow, and authorizes dispatch.")
    @PostMapping("/webhooks/bank-reconciliation")
    public ResponseEntity<BankReconciliationWebhookResponse> bankReconciliationWebhook(
            @Valid @RequestBody BankReconciliationWebhookRequest request,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        BankReconciliationWebhookResponse response = bankReconciliationService.processReconciliation(request, webhookSecret, clientIp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "6. Upload Letter of Credit (LC - JSON)", description = "Uploads Letter of Credit document metadata via JSON. Stored in SUBMITTED status. Protected buyer.")
    @PostMapping(value = "/lc/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LcUploadResponse> uploadLetterOfCreditJson(
            @Valid @RequestBody LcUploadRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        LcUploadResponse response = letterOfCreditService.uploadLetterOfCredit(request, null, clientIp);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "6. Upload Letter of Credit (LC - Multipart)", description = "Uploads Letter of Credit PDF document file and metadata. Stored in SUBMITTED status. Protected buyer.")
    @PostMapping(value = "/lc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LcUploadResponse> uploadLetterOfCreditMultipart(
            @Valid @ModelAttribute LcUploadRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        LcUploadResponse response = letterOfCreditService.uploadLetterOfCredit(request, file, clientIp);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "7. Fetch Letter of Credit Status (:orderId)", description = "Fetches LC verification status and remarks for an order. Protected buyer/seller.")
    @GetMapping("/lc/{orderId}/status")
    public ResponseEntity<LcStatusResponse> getLetterOfCreditStatus(
            @Parameter(description = "Order ID in :orderId notation") @PathVariable String orderId) {

        LcStatusResponse response = letterOfCreditService.getLetterOfCreditStatus(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "8. Verify Letter of Credit (:orderId)", description = "Finance/Admin approves or rejects LC. On approval, locks escrow and authorizes dispatch.")
    @PutMapping("/lc/{orderId}/verify")
    public ResponseEntity<LcStatusResponse> verifyLetterOfCredit(
            @Parameter(description = "Order ID in :orderId notation") @PathVariable String orderId,
            @Valid @RequestBody LcVerificationRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        LcStatusResponse response = letterOfCreditService.verifyLetterOfCredit(orderId, request, clientIp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "13. Raise Payment Dispute", description = "Raises a formal dispute, immediately freezes escrow funds in DISPUTED state, and emits PaymentDisputedEvent.")
    @PostMapping("/dispute")
    public ResponseEntity<com.payment.dto.dispute.DisputeResponse> raiseDispute(
            @Valid @RequestBody com.payment.dto.dispute.RaiseDisputeRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        com.payment.dto.dispute.DisputeResponse response = disputeService.raiseDispute(request, clientIp);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "14. Process Refund & Escrow Reversal", description = "Executes full/partial refund, reverses escrow to REFUNDED_TO_BUYER, calls payment gateway refund API, and resolves any active disputes.")
    @PostMapping("/refund")
    public ResponseEntity<com.payment.dto.refund.RefundResponse> processRefund(
            @Valid @RequestBody com.payment.dto.refund.ExecuteRefundRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getRemoteAddr();
        com.payment.dto.refund.RefundResponse response = refundService.processRefund(request, clientIp);
        return ResponseEntity.ok(response);
    }
}
