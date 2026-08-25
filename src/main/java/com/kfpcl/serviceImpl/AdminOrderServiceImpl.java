package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.Order;
import com.kfpcl.entity.OrderTracking;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.OrderItemRepository;
import com.kfpcl.repository.OrderRepository;
import com.kfpcl.repository.OrderTrackingRepository;
import com.kfpcl.service.AdminOrderService;
import com.kfpcl.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponseDto> getOrders(String search, String status, String paymentStatus, String region, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                try {
                    Order.OrderStatus oStatus = Order.OrderStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("orderStatus"), oStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(paymentStatus)) {
                try {
                    Order.PaymentStatus pStatus = Order.PaymentStatus.valueOf(paymentStatus.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("paymentStatus"), pStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(region)) {
                predicates.add(cb.equal(cb.lower(root.get("region")), region.trim().toLowerCase()));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("orderNumber")), pattern);
                Predicate buyerMatch = cb.like(cb.lower(root.get("buyerName")), pattern);
                Predicate sellerMatch = cb.like(cb.lower(root.get("sellerName")), pattern);
                predicates.add(cb.or(numMatch, buyerMatch, sellerMatch));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        List<OrderResponseDto> dtoList = orderPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(orderPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return mapToDto(order);
    }

    @Override
    public OrderResponseDto updateOrderStatus(String orderId, OrderStatusUpdateDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid order status: " + dto.getStatus());
        }

        String oldStatus = order.getOrderStatus().name();
        order.setOrderStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Add auto-tracking entry for status transition
        OrderTracking tracking = OrderTracking.builder()
                .id("trk_" + UUID.randomUUID().toString().substring(0, 8))
                .orderId(order.getId())
                .carrier("KFPCL Logistics")
                .trackingNumber("KFP-TRK-" + order.getOrderNumber())
                .status("Status updated to " + newStatus.name())
                .remarks(dto.getRemarks() != null ? dto.getRemarks() : "Order status transition by Admin")
                .build();
        orderTrackingRepository.save(tracking);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_ORDER_STATUS", "ORDER", orderId, oldStatus, newStatus.name(), null, null);

        return mapToDto(saved);
    }

    @Override
    public OrderTrackingResponseDto addOrderTracking(String orderId, OrderTrackingCreateDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        OrderTracking tracking = OrderTracking.builder()
                .id("trk_" + UUID.randomUUID().toString().substring(0, 8))
                .orderId(order.getId())
                .carrier(dto.getCarrier() != null ? dto.getCarrier() : "Standard Freight")
                .trackingNumber(StringUtils.hasText(dto.getTrackingNumber()) ? dto.getTrackingNumber() : "KFP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(dto.getStatus().trim())
                .location(dto.getLocation())
                .remarks(dto.getRemarks())
                .estimatedDelivery(dto.getEstimatedDelivery())
                .build();

        OrderTracking saved = orderTrackingRepository.save(tracking);
        auditLogService.logAction("admin", "ROLE_ADMIN", "ADD_ORDER_TRACKING", "ORDER_TRACKING", orderId, null, saved.getStatus(), null, null);

        return OrderTrackingResponseDto.builder()
                .id(saved.getId())
                .orderId(saved.getOrderId())
                .carrier(saved.getCarrier())
                .trackingNumber(saved.getTrackingNumber())
                .status(saved.getStatus())
                .location(saved.getLocation())
                .remarks(saved.getRemarks())
                .estimatedDelivery(saved.getEstimatedDelivery())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportOrdersCsv() {
        List<Order> orders = orderRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("OrderId,OrderNumber,BuyerName,SellerName,TotalAmount,Discount,FinalAmount,PaymentStatus,OrderStatus,Region,CreatedAt\n");

        for (Order o : orders) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    o.getId(),
                    o.getOrderNumber(),
                    o.getBuyerName() != null ? o.getBuyerName() : "",
                    o.getSellerName() != null ? o.getSellerName() : "",
                    o.getTotalAmount(),
                    o.getDiscountAmount() != null ? o.getDiscountAmount() : 0.0,
                    o.getFinalAmount(),
                    o.getPaymentStatus().name(),
                    o.getOrderStatus().name(),
                    o.getRegion() != null ? o.getRegion() : "",
                    o.getCreatedAt() != null ? o.getCreatedAt().toString() : ""));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private OrderResponseDto mapToDto(Order order) {
        List<OrderItemDto> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(i -> OrderItemDto.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .sku(i.getSku())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        List<OrderTrackingResponseDto> tracking = orderTrackingRepository.findByOrderIdOrderByCreatedAtDesc(order.getId()).stream()
                .map(t -> OrderTrackingResponseDto.builder()
                        .id(t.getId())
                        .orderId(t.getOrderId())
                        .carrier(t.getCarrier())
                        .trackingNumber(t.getTrackingNumber())
                        .status(t.getStatus())
                        .location(t.getLocation())
                        .remarks(t.getRemarks())
                        .estimatedDelivery(t.getEstimatedDelivery())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .buyerName(order.getBuyerName())
                .sellerId(order.getSellerId())
                .sellerName(order.getSellerName())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .finalAmount(order.getFinalAmount())
                .paymentStatus(order.getPaymentStatus().name())
                .orderStatus(order.getOrderStatus().name())
                .shippingAddress(order.getShippingAddress())
                .region(order.getRegion())
                .items(items)
                .trackingHistory(tracking)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
