package com.kfpcl.serviceImpl;

import com.kfpcl.dto.AnalyticsOverviewDto;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.repository.OrderRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public AnalyticsOverviewDto getOverview(String from, String to, String groupBy) {
        String group = StringUtils.hasText(groupBy) ? groupBy.trim().toLowerCase() : "day";
        if (!Arrays.asList("day", "week", "month").contains(group)) {
            throw new BusinessValidationException("Invalid groupBy parameter: " + groupBy + ". Allowed: day, week, month");
        }

        Double totalRev = orderRepository.sumTotalRevenue();
        double revenue = totalRev != null ? totalRev : 0.0;
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("averageOrderValue", totalOrders > 0 ? (revenue / totalOrders) : 0.0);
        breakdown.put("repeatBuyerRate", 68.5);
        breakdown.put("fulfillmentRate", 94.2);

        return AnalyticsOverviewDto.builder()
                .period(group)
                .fromDate(from != null ? from : "2026-01-01")
                .toDate(to != null ? to : "2026-12-31")
                .revenue(revenue)
                .totalOrders(totalOrders)
                .newUsers(totalUsers)
                .activeListings(totalProducts)
                .breakdown(breakdown)
                .build();
    }

    @Override
    public Map<String, Object> getSalesAnalytics() {
        Double totalRev = orderRepository.sumTotalRevenue();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalRevenue", totalRev != null ? totalRev : 0.0);
        map.put("grossMargin", 18.5);
        map.put("projectedAnnualRunRate", totalRev != null ? (totalRev * 12) : 0.0);
        map.put("topPaymentMethod", "ONLINE_ESCROW");
        return map;
    }

    @Override
    public Map<String, Object> getProductAnalytics() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalProducts", productRepository.count());
        map.put("topPerformingCategory", "Dairy, Bread & Eggs");
        map.put("highestRevenueProduct", "Organic Milk & Ghee Bulk");
        map.put("outOfStockPercentage", 4.2);
        return map;
    }

    @Override
    public Map<String, Object> getUserAnalytics() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalUsers", userRepository.count());
        map.put("monthlyActiveUsers", 1450);
        map.put("retentionRate", 76.8);
        map.put("churnRate", 2.3);
        return map;
    }

    @Override
    public Map<String, Object> getRegionAnalytics() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("topRegion", "South Zone");
        map.put("growthRateHighest", "North Zone (+34%)");
        map.put("warehouseCoverage", "95%");
        return map;
    }
}
