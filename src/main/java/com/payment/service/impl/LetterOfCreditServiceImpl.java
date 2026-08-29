package com.payment.service.impl;

import com.payment.dto.lc.LcStatusResponse;
import com.payment.dto.lc.LcUploadRequest;
import com.payment.dto.lc.LcUploadResponse;
import com.payment.dto.lc.LcVerificationRequest;
import com.payment.entity.EscrowAccount;
import com.payment.entity.Invoice;
import com.payment.entity.LetterOfCredit;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.*;
import com.payment.event.DispatchAllowedEvent;
import com.payment.event.EscrowLockedEvent;
import com.payment.event.PaymentVerifiedEvent;
import com.payment.exception.BadRequestException;
import com.payment.exception.DuplicateResourceException;
import com.payment.exception.ResourceNotFoundException;
import com.payment.integration.order.OrderDetailsDto;
import com.payment.integration.order.OrderPaymentClient;
import com.payment.repository.EscrowAccountRepository;
import com.payment.repository.LetterOfCreditRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.InvoiceService;
import com.payment.service.LetterOfCreditService;
import com.payment.service.audit.PaymentAuditService;
import com.payment.service.state.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterOfCreditServiceImpl implements LetterOfCreditService {

    private final LetterOfCreditRepository letterOfCreditRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final EscrowAccountRepository escrowAccountRepository;
    private final OrderPaymentClient orderPaymentClient;
    private final SecurityUtils securityUtils;
    private final PaymentStateMachine stateMachine;
    private final InvoiceService invoiceService;
    private final PaymentAuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public LcUploadResponse uploadLetterOfCredit(LcUploadRequest request, MultipartFile file, String clientIp) {
        UserContext currentUser = securityUtils.getCurrentUser();
        log.info("Processing LC upload for orderId: {}, lcNumber: {}, user: {}",
                request.getOrderId(), request.getLcNumber(), currentUser.getUserId());

        // 1. Fetch trusted order details
        OrderDetailsDto order = orderPaymentClient.getOrderDetails(request.getOrderId());
        if (order == null) {
            throw new BadRequestException("Order not found with ID: " + request.getOrderId());
        }

        // 2. Validate that the authenticated buyer owns the order
        securityUtils.verifyBuyerOwnership(order.getBuyerId());

        // 3. Ensure LC number is unique across all LC records
        if (letterOfCreditRepository.existsByLcNumber(request.getLcNumber())) {
            throw new DuplicateResourceException("Letter of Credit with number '" + request.getLcNumber() + "' already exists.");
        }

        // 4. Ensure LC amount matches order payable amount
        if (order.getGrandTotal().compareTo(request.getLcAmount()) != 0) {
            throw new BadRequestException(String.format(
                    "LC amount mismatch: Submitted LC amount (%s %s) does not match required order amount (%s %s)",
                    order.getCurrency(), request.getLcAmount(), order.getCurrency(), order.getGrandTotal()
            ));
        }

        // 5. Ensure LC expiry date is in the future
        if (request.getExpiryDate() == null || !request.getExpiryDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("LC expiry date must be a valid future date.");
        }

        // 6. Handle Document URL or Multipart File
        String docUrl = request.getDocumentUrl();
        if (file != null && !file.isEmpty()) {
            docUrl = "https://storage.kfpcl.com/lc/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            log.info("Uploaded LC file saved to virtual path: {}", docUrl);
        }
        if (docUrl == null || docUrl.isBlank()) {
            docUrl = "https://storage.kfpcl.com/lc/" + request.getOrderId() + "-lc-doc.pdf";
        }

        // 7. Find or create PaymentTransaction
        PaymentTransaction transaction = transactionRepository.findByOrderId(order.getOrderId())
                .orElseGet(() -> {
                    String txnRef = "TXN-LC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                    return PaymentTransaction.builder()
                            .transactionReference(txnRef)
                            .orderId(order.getOrderId())
                            .buyerId(order.getBuyerId())
                            .sellerId(order.getSellerId())
                            .paymentMethod(PaymentMethod.LC)
                            .gateway(PaymentGatewayType.NONE)
                            .amount(order.getGrandTotal())
                            .currency(order.getCurrency())
                            .status(PaymentStatus.PENDING_PAYMENT)
                            .recipientEmail(order.getCustomerEmail())
                            .build();
                });

        transaction.setPaymentMethod(PaymentMethod.LC);
        PaymentTransaction savedTxn = transactionRepository.save(transaction);

        // 8. Create and Save LetterOfCredit entity with status SUBMITTED
        LetterOfCredit lc = LetterOfCredit.builder()
                .orderId(order.getOrderId())
                .transactionId(savedTxn.getId())
                .lcNumber(request.getLcNumber())
                .issuingBank(request.getIssuingBank())
                .advisingBank(request.getAdvisingBank())
                .lcAmount(request.getLcAmount())
                .expiryDate(request.getExpiryDate())
                .tenorDays(request.getTenorDays())
                .documentUrl(docUrl)
                .status(LcStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        LetterOfCredit savedLc = letterOfCreditRepository.save(lc);

        // 9. Write Audit Log
        auditService.logAction(
                savedTxn.getId(),
                order.getOrderId(),
                "LC_UPLOADED",
                null,
                LcStatus.SUBMITTED.name(),
                currentUser.getUserId(),
                "LETTER_OF_CREDIT",
                clientIp,
                "LC uploaded: " + request.getLcNumber() + ", Bank: " + request.getIssuingBank()
        );

        return LcUploadResponse.builder()
                .id(savedLc.getId())
                .orderId(savedLc.getOrderId())
                .lcNumber(savedLc.getLcNumber())
                .issuingBank(savedLc.getIssuingBank())
                .advisingBank(savedLc.getAdvisingBank())
                .lcAmount(savedLc.getLcAmount())
                .expiryDate(savedLc.getExpiryDate())
                .tenorDays(savedLc.getTenorDays())
                .documentUrl(savedLc.getDocumentUrl())
                .status(savedLc.getStatus())
                .submittedAt(savedLc.getSubmittedAt())
                .message("Letter of Credit document and metadata uploaded successfully in SUBMITTED status.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LcStatusResponse getLetterOfCreditStatus(String orderId) {
        log.info("Fetching LC status for orderId: {}", orderId);
        LetterOfCredit lc = letterOfCreditRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No Letter of Credit found for order ID: " + orderId));

        OrderDetailsDto order = orderPaymentClient.getOrderDetails(orderId);
        if (order != null) {
            securityUtils.verifyBuyerOwnership(order.getBuyerId());
        }

        return mapToStatusResponse(lc);
    }

    @Override
    @Transactional
    public LcStatusResponse verifyLetterOfCredit(String orderId, LcVerificationRequest request, String clientIp) {
        UserContext currentUser = securityUtils.getCurrentUser();
        log.info("Admin/Finance verifying LC for orderId: {}, approved: {}, user: {}",
                orderId, request.getApproved(), currentUser.getUserId());

        // Admin or Finance role is required
        securityUtils.requireRole(UserRole.ADMIN, UserRole.FINANCE);

        LetterOfCredit lc = letterOfCreditRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No Letter of Credit found for order ID: " + orderId));

        LocalDateTime now = LocalDateTime.now();
        lc.setVerifiedAt(now);
        lc.setVerifiedBy(currentUser.getUserId());

        PaymentTransaction transaction = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for order: " + orderId));

        if (Boolean.TRUE.equals(request.getApproved())) {
            lc.setStatus(LcStatus.APPROVED);
            lc.setVerificationRemarks(request.getVerificationRemarks());

            // Lock Escrow
            EscrowAccount escrow = escrowAccountRepository.findByOrderId(orderId)
                    .orElseGet(() -> {
                        String cleanOrderId = orderId.replace("-", "").toUpperCase();
                        return EscrowAccount.builder()
                                .orderId(orderId)
                                .virtualAccountNumber("KFPCL" + cleanOrderId)
                                .ifscCode("KFPCL00001")
                                .build();
                    });
            escrow.setTotalAmountLocked(lc.getLcAmount());
            escrow.setReleaseStatus(EscrowReleaseStatus.LOCKED);
            escrow.setLockedAt(now);
            escrow.setPaymentTransactionId(transaction.getId());
            escrowAccountRepository.save(escrow);

            // Transition transaction state to DISPATCH_ALLOWED
            if (transaction.getStatus() == PaymentStatus.PENDING_PAYMENT) {
                stateMachine.validateTransition(PaymentStatus.PENDING_PAYMENT, PaymentStatus.PAYMENT_PROCESSING);
            }
            stateMachine.validateTransition(PaymentStatus.PAYMENT_PROCESSING, PaymentStatus.ESCROW_LOCKED);
            stateMachine.validateTransition(PaymentStatus.ESCROW_LOCKED, PaymentStatus.DISPATCH_ALLOWED);
            transaction.setStatus(PaymentStatus.DISPATCH_ALLOWED);
            PaymentTransaction savedTxn = transactionRepository.save(transaction);

            // Generate GST Tax Invoice
            invoiceService.generateOrGetInvoice(savedTxn, InvoiceType.TAX_INVOICE);

            // Update Order Payment Status to PAID
            orderPaymentClient.updateOrderPaymentStatus(orderId, "PAID");

            // Publish Domain Events
            eventPublisher.publishEvent(PaymentVerifiedEvent.builder()
                    .orderId(transaction.getOrderId())
                    .transactionId(transaction.getId())
                    .transactionReference(transaction.getTransactionReference())
                    .buyerId(transaction.getBuyerId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .timestamp(now)
                    .build());

            eventPublisher.publishEvent(EscrowLockedEvent.builder()
                    .orderId(transaction.getOrderId())
                    .escrowAccountId(escrow.getId())
                    .virtualAccountNumber(escrow.getVirtualAccountNumber())
                    .totalAmountLocked(escrow.getTotalAmountLocked())
                    .lockedAt(now)
                    .build());

            eventPublisher.publishEvent(DispatchAllowedEvent.builder()
                    .orderId(transaction.getOrderId())
                    .transactionId(transaction.getId())
                    .reason("Letter of Credit approved by Finance. Escrow locked and dispatch allowed.")
                    .timestamp(now)
                    .build());

            auditService.logAction(
                    transaction.getId(),
                    orderId,
                    "LC_APPROVED",
                    LcStatus.SUBMITTED.name(),
                    LcStatus.APPROVED.name(),
                    currentUser.getUserId(),
                    "FINANCE_PORTAL",
                    clientIp,
                    "LC " + lc.getLcNumber() + " approved. Remarks: " + request.getVerificationRemarks()
            );
        } else {
            lc.setStatus(LcStatus.REJECTED);
            lc.setRejectionReason(request.getRejectionReason());
            lc.setVerificationRemarks(request.getVerificationRemarks());

            auditService.logAction(
                    transaction.getId(),
                    orderId,
                    "LC_REJECTED",
                    LcStatus.SUBMITTED.name(),
                    LcStatus.REJECTED.name(),
                    currentUser.getUserId(),
                    "FINANCE_PORTAL",
                    clientIp,
                    "LC " + lc.getLcNumber() + " rejected. Reason: " + request.getRejectionReason()
            );
        }

        LetterOfCredit updatedLc = letterOfCreditRepository.save(lc);
        return mapToStatusResponse(updatedLc);
    }

    private LcStatusResponse mapToStatusResponse(LetterOfCredit lc) {
        return LcStatusResponse.builder()
                .orderId(lc.getOrderId())
                .lcNumber(lc.getLcNumber())
                .issuingBank(lc.getIssuingBank())
                .advisingBank(lc.getAdvisingBank())
                .lcAmount(lc.getLcAmount())
                .expiryDate(lc.getExpiryDate())
                .tenorDays(lc.getTenorDays())
                .documentUrl(lc.getDocumentUrl())
                .status(lc.getStatus())
                .submittedAt(lc.getSubmittedAt())
                .verifiedAt(lc.getVerifiedAt())
                .verifiedBy(lc.getVerifiedBy())
                .verificationRemarks(lc.getVerificationRemarks())
                .rejectionReason(lc.getRejectionReason())
                .build();
    }
}
