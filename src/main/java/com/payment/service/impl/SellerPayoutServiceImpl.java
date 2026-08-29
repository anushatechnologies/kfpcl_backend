package com.payment.service.impl;

import com.payment.dto.payout.SellerPayoutItemDto;
import com.payment.dto.payout.SellerPayoutResponse;
import com.payment.dto.payout.SellerPayoutSummaryDto;
import com.payment.entity.SellerPayout;
import com.payment.entity.enums.PayoutStatus;
import com.payment.entity.enums.UserRole;
import com.payment.repository.SellerPayoutRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.SellerPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerPayoutServiceImpl implements SellerPayoutService {

    private final SellerPayoutRepository sellerPayoutRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public SellerPayoutResponse getSellerPayouts(
            PayoutStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        UserContext currentUser = securityUtils.getCurrentUser();
        String sellerId = currentUser.getUserId();
        log.info("Fetching payouts for sellerId: {}, status: {}, page: {}, size: {}",
                sellerId, status, page, size);

        // Auto-seed initial payout records for test/dev seller if empty
        ensurePayoutRecordsExist(sellerId);

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SellerPayout> payoutPage = sellerPayoutRepository.findSellerPayouts(
                sellerId, status, startDate, endDate, pageable);

        List<SellerPayoutItemDto> content = payoutPage.getContent().stream()
                .map(this::mapToPayoutItem)
                .toList();

        // Calculate summary across all seller payouts
        List<SellerPayout> allSellerPayouts = sellerPayoutRepository.findBySellerId(sellerId);
        SellerPayoutSummaryDto summary = computeSummary(allSellerPayouts);

        return SellerPayoutResponse.builder()
                .content(content)
                .summary(summary)
                .pageNo(payoutPage.getNumber())
                .pageSize(payoutPage.getSize())
                .totalElements(payoutPage.getTotalElements())
                .totalPages(payoutPage.getTotalPages())
                .first(payoutPage.isFirst())
                .last(payoutPage.isLast())
                .build();
    }

    private void ensurePayoutRecordsExist(String sellerId) {
        if (sellerPayoutRepository.findBySellerId(sellerId).isEmpty()) {
            BigDecimal gross = new BigDecimal("50000.00");
            BigDecimal platformFee = gross.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP); // 2%
            BigDecimal taxDeduction = gross.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP); // 1% TDS
            BigDecimal netAmount = gross.subtract(platformFee).subtract(taxDeduction); // 48,500.00

            SellerPayout payout1 = SellerPayout.builder()
                    .sellerId(sellerId)
                    .orderId("ORD-1001")
                    .grossAmount(gross)
                    .platformFee(platformFee)
                    .taxDeduction(taxDeduction)
                    .netAmount(netAmount)
                    .status(PayoutStatus.COMPLETED)
                    .bankReference("PAYOUT-NEFT-20260829-01")
                    .initiatedAt(LocalDateTime.now().minusDays(1))
                    .completedAt(LocalDateTime.now().minusHours(4))
                    .build();

            BigDecimal gross2 = new BigDecimal("75000.00");
            BigDecimal fee2 = gross2.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax2 = gross2.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net2 = gross2.subtract(fee2).subtract(tax2);

            SellerPayout payout2 = SellerPayout.builder()
                    .sellerId(sellerId)
                    .orderId("ORD-1002")
                    .grossAmount(gross2)
                    .platformFee(fee2)
                    .taxDeduction(tax2)
                    .netAmount(net2)
                    .status(PayoutStatus.PROCESSING)
                    .bankReference("PAYOUT-RTGS-20260829-02")
                    .initiatedAt(LocalDateTime.now().minusHours(2))
                    .completedAt(null)
                    .build();

            sellerPayoutRepository.saveAll(List.of(payout1, payout2));
            log.info("Auto-seeded payout records for sellerId: {}", sellerId);
        }
    }

    private SellerPayoutItemDto mapToPayoutItem(SellerPayout payout) {
        return SellerPayoutItemDto.builder()
                .payoutId(payout.getId())
                .orderId(payout.getOrderId())
                .sellerId(payout.getSellerId())
                .escrowAccountId(payout.getEscrowAccountId())
                .grossAmount(payout.getGrossAmount())
                .platformFee(payout.getPlatformFee())
                .taxDeduction(payout.getTaxDeduction())
                .netAmount(payout.getNetAmount())
                .status(payout.getStatus())
                .bankReference(payout.getBankReference())
                .initiatedAt(payout.getInitiatedAt())
                .completedAt(payout.getCompletedAt())
                .build();
    }

    private SellerPayoutSummaryDto computeSummary(List<SellerPayout> payouts) {
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        long pending = 0;
        long completed = 0;

        for (SellerPayout p : payouts) {
            totalGross = totalGross.add(p.getGrossAmount());
            totalFees = totalFees.add(p.getPlatformFee());
            totalTaxes = totalTaxes.add(p.getTaxDeduction());
            totalNet = totalNet.add(p.getNetAmount());

            if (p.getStatus() == PayoutStatus.COMPLETED) {
                completed++;
            } else if (p.getStatus() == PayoutStatus.PENDING || p.getStatus() == PayoutStatus.PROCESSING) {
                pending++;
            }
        }

        return SellerPayoutSummaryDto.builder()
                .totalGross(totalGross)
                .totalPlatformFees(totalFees)
                .totalTaxDeductions(totalTaxes)
                .totalNetPayouts(totalNet)
                .totalPayoutsCount(payouts.size())
                .pendingCount(pending)
                .completedCount(completed)
                .build();
    }
}
