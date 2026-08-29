package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.SellerDashboardStatsResponse;
import com.kfpcl.entity.Inquiry;
import com.kfpcl.repository.InquiryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.service.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final ProductRepository productRepository;
    private final InquiryRepository inquiryRepository;
    private final QuotationRepository quotationRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardStatsResponse getDashboardStats(String sellerId) {
        // Simplified metrics for the dashboard
        long openInquiries = inquiryRepository.findBySellerIdAndStatus(sellerId, Inquiry.Status.PENDING, PageRequest.of(0, 100)).getTotalElements();
        long submittedQuotes = quotationRepository.findBySellerId(sellerId, PageRequest.of(0, 100)).getTotalElements();

        return SellerDashboardStatsResponse.builder()
                .totalProducts(0) // Requires sellerId on product query logic
                .activeProducts(0) 
                .openInquiries(openInquiries)
                .submittedQuotes(submittedQuotes)
                .totalRevenue(0.0) 
                .build();
    }
}
