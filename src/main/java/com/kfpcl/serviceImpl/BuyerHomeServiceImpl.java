package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Order;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.User;
import com.kfpcl.repository.OrderRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.repository.WishlistRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerHomeServiceImpl implements BuyerHomeService {

    private final CatalogService catalogService;
    private final RecommendationService recommendationService;
    private final RecentlyViewedService recentlyViewedService;
    private final WishlistRepository wishlistRepository;
    private final RfqRepository rfqRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public BuyerHomeResponse getBuyerHomeDashboard() {
        Buyer buyer = securityUtils.getCurrentBuyer();
        User user = buyer.getUser();

        BuyerSummaryResponse buyerSummary = BuyerSummaryResponse.builder()
                .buyerId(buyer.getId())
                .name(user != null ? user.getName() : null)
                .email(user != null ? user.getEmail() : null)
                .companyName(buyer.getCompanyName())
                .businessType(buyer.getBusinessType())
                .gstNumber(buyer.getGstNumber())
                .build();

        long wishlistCount = wishlistRepository.countByBuyerId(buyer.getId());
        long activeRfqsCount = rfqRepository.countByBuyerIdAndStatusIn(buyer.getId(), List.of(Rfq.Status.OPEN, Rfq.Status.QUOTATIONS_RECEIVED));
        long totalOrdersCount = orderRepository.countByBuyerId(buyer.getId());

        List<ProductResponse> featured = catalogService.getFeaturedProducts();
        List<ProductResponse> recommendations = recommendationService.getBuyerRecommendations();
        List<ProductResponse> recentlyViewed = recentlyViewedService.getRecentlyViewed();

        List<RfqSummaryResponse> activeRfqs = rfqRepository.findTop5ByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(this::mapToRfqSummary)
                .collect(Collectors.toList());

        List<BuyerOrderSummaryResponse> recentOrders = orderRepository.findTop5ByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(this::mapToOrderSummary)
                .collect(Collectors.toList());

        return BuyerHomeResponse.builder()
                .buyer(buyerSummary)
                .wishlistCount(wishlistCount)
                .activeRfqsCount(activeRfqsCount)
                .totalOrdersCount(totalOrdersCount)
                .featuredProducts(featured)
                .recommendations(recommendations)
                .recentlyViewed(recentlyViewed)
                .activeRfqs(activeRfqs)
                .recentOrders(recentOrders)
                .build();
    }

    private RfqSummaryResponse mapToRfqSummary(Rfq rfq) {
        return RfqSummaryResponse.builder()
                .id(rfq.getId())
                .productTitle(rfq.getProductTitle())
                .quantity(rfq.getQuantity())
                .unit(rfq.getUnit())
                .targetPrice(rfq.getTargetPrice())
                .expectedDeliveryDate(rfq.getExpectedDeliveryDate())
                .status(rfq.getStatus().name())
                .createdAt(rfq.getCreatedAt())
                .build();
    }

    private BuyerOrderSummaryResponse mapToOrderSummary(Order order) {
        return BuyerOrderSummaryResponse.builder()
                .id(order.getId())
                .supplierName(order.getSupplier() != null ? order.getSupplier().getCompanyName() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .totalItems(order.getItems() != null ? order.getItems().size() : 0)
                .estimatedDelivery(order.getEstimatedDelivery())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
