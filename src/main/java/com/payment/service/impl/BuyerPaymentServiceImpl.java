package com.payment.service.impl;

import com.payment.dto.history.PaymentHistoryItemDto;
import com.payment.dto.history.PaymentHistoryResponse;
import com.payment.entity.Invoice;
import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.UserRole;
import com.payment.repository.InvoiceRepository;
import com.payment.repository.PaymentTransactionRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.BuyerPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerPaymentServiceImpl implements BuyerPaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PaymentHistoryResponse getBuyerPaymentHistory(
            PaymentStatus status,
            PaymentMethod paymentMethod,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        UserContext currentUser = securityUtils.getCurrentUser();
        String buyerId = currentUser.getUserId();
        log.info("Fetching payment history for buyerId: {}, status: {}, method: {}, page: {}, size: {}",
                buyerId, status, paymentMethod, page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentTransaction> txnPage = transactionRepository.findBuyerTransactions(
                buyerId, status, paymentMethod, startDate, endDate, pageable);

        List<PaymentHistoryItemDto> content = txnPage.getContent().stream()
                .map(this::mapToHistoryItem)
                .toList();

        return PaymentHistoryResponse.builder()
                .content(content)
                .pageNo(txnPage.getNumber())
                .pageSize(txnPage.getSize())
                .totalElements(txnPage.getTotalElements())
                .totalPages(txnPage.getTotalPages())
                .first(txnPage.isFirst())
                .last(txnPage.isLast())
                .build();
    }

    private PaymentHistoryItemDto mapToHistoryItem(PaymentTransaction txn) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(txn.getOrderId()).stream().findFirst();

        String invoiceNumber = invoiceOpt.map(Invoice::getInvoiceNumber).orElse(null);
        String downloadUrl = invoiceOpt.isPresent()
                ? "/api/v1/orders/" + txn.getOrderId() + "/proforma-invoice"
                : null;

        return PaymentHistoryItemDto.builder()
                .transactionReference(txn.getTransactionReference())
                .orderId(txn.getOrderId())
                .sellerId(txn.getSellerId())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .paymentMethod(txn.getPaymentMethod())
                .gateway(txn.getGateway())
                .status(txn.getStatus())
                .utrNumber(txn.getUtrNumber())
                .invoiceNumber(invoiceNumber)
                .invoiceDownloadUrl(downloadUrl)
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }
}
