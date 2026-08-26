package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.SellerDashboardStatsResponse;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.InquiryStatus;
import com.kfpcl.entity.enums.ProductStatus;
import com.kfpcl.entity.enums.QuotationStatus;
import com.kfpcl.repository.InquiryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.service.SellerDashboardService;
import com.kfpcl.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final SellerProfileService sellerProfileService;
    private final ProductRepository productRepository;
    private final InquiryRepository inquiryRepository;
    private final QuotationRepository quotationRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardStatsResponse getSellerDashboardStats(String sellerEmail) {
        Seller seller = sellerProfileService.getSellerEntityByEmail(sellerEmail);
        Long sellerId = seller.getId();

        // 1. Product Stats
        long totalProducts = productRepository.countBySellerId(sellerId);
        long activeProducts = productRepository.countBySellerIdAndStatus(sellerId, ProductStatus.ACTIVE);
        long pendingApproval = productRepository.countBySellerIdAndStatus(sellerId, ProductStatus.PENDING_APPROVAL);
        long draftProducts = productRepository.countBySellerIdAndStatus(sellerId, ProductStatus.DRAFT);
        long archivedProducts = productRepository.countBySellerIdAndStatus(sellerId, ProductStatus.ARCHIVED);
        Long totalViews = productRepository.sumViewCountBySellerId(sellerId);

        // 2. Inquiry / Lead Stats
        long pendingInquiries = inquiryRepository.countBySellerIdAndStatus(sellerId, InquiryStatus.PENDING);
        long repliedInquiries = inquiryRepository.countBySellerIdAndStatus(sellerId, InquiryStatus.REPLIED);
        long closedInquiries = inquiryRepository.countBySellerIdAndStatus(sellerId, InquiryStatus.CLOSED);
        long totalInquiries = pendingInquiries + repliedInquiries + closedInquiries;

        // 3. Quotation & Commercial Stats
        long totalQuotes = quotationRepository.countBySellerId(sellerId);
        long openQuotes = quotationRepository.countBySellerIdAndStatus(sellerId, QuotationStatus.SUBMITTED);
        long acceptedQuotes = quotationRepository.countBySellerIdAndStatus(sellerId, QuotationStatus.ACCEPTED);
        long closedQuotes = quotationRepository.countBySellerIdAndStatus(sellerId, QuotationStatus.CLOSED);
        long rejectedQuotes = quotationRepository.countBySellerIdAndStatus(sellerId, QuotationStatus.REJECTED);
        BigDecimal awardedRevenue = quotationRepository.sumAwardedRevenueBySellerId(sellerId);

        Double winRate = 0.0;
        if (totalQuotes > 0) {
            winRate = Math.round(((double) acceptedQuotes / totalQuotes * 100.0) * 10.0) / 10.0;
        }

        return SellerDashboardStatsResponse.builder()
                .sellerId(seller.getId())
                .companyName(seller.getCompanyName())
                .isVerified(seller.getIsVerified())
                .verificationStatus(seller.getVerificationStatus())
                .isAllowedToPublish(seller.isAllowedToPublish())
                .rating(seller.getRating())
                .totalReviews(seller.getTotalReviews())
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .pendingApprovalProducts(pendingApproval)
                .draftProducts(draftProducts)
                .archivedProducts(archivedProducts)
                .totalCatalogViews(totalViews != null ? totalViews : 0L)
                .totalInquiries(totalInquiries)
                .pendingInquiries(pendingInquiries)
                .repliedInquiries(repliedInquiries)
                .totalQuotesSubmitted(totalQuotes)
                .activeOpenQuotesCount(openQuotes)
                .acceptedQuotesCount(acceptedQuotes)
                .closedOrRejectedQuotesCount(closedQuotes + rejectedQuotes)
                .totalOrderRevenueAwarded(awardedRevenue != null ? awardedRevenue : BigDecimal.ZERO)
                .quotationWinRatePercentage(winRate)
                .build();
    }
}
