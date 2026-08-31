package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.Order;
import com.kfpcl.entity.OrderTracking;
import com.kfpcl.entity.OrderStatus;
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
                    OrderStatus oStatus = OrderStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), oStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(paymentStatus)) {
                predicates.add(cb.equal(cb.lower(root.get("paymentStatus")), paymentStatus.trim().toLowerCase()));
            }

            if (StringUtils.hasText(region)) {
                predicates.add(cb.like(cb.lower(root.get("shippingAddress")), "%" + region.trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("orderNumber")), pattern);
                Predicate buyerMatch = cb.like(cb.lower(root.get("buyerId")), pattern);
                Predicate sellerMatch = cb.like(cb.lower(root.get("sellerId")), pattern);
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
        Order order = orderRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return mapToDto(order);
    }

    @Override
    public OrderResponseDto updateOrderStatus(String orderId, OrderStatusUpdateDto dto) {
        Order order = orderRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        OrderStatus newStatus = dto.getStatus();
        if (newStatus == null) {
            throw new BusinessValidationException("Status cannot be null");
        }

        String oldStatus = order.getStatus().name();
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        OrderTracking tracking = OrderTracking.builder()
                .id("trk_" + UUID.randomUUID().toString().substring(0, 8))
                .orderId(String.valueOf(order.getId()))
                .carrier("KFPCL Logistics")
                .trackingNumber("KFP-TRK-" + order.getOrderNumber())
                .status("Status updated to " + newStatus.name())
                .remarks("Order status transition by Admin")
                .build();
        orderTrackingRepository.save(tracking);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_ORDER_STATUS", "ORDER", orderId, oldStatus, newStatus.name(), null, null);

        return mapToDto(saved);
    }

    @Override
    public OrderTrackingResponseDto addOrderTracking(String orderId, OrderTrackingCreateDto dto) {
        Order order = orderRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        OrderTracking tracking = OrderTracking.builder()
                .id("trk_" + UUID.randomUUID().toString().substring(0, 8))
                .orderId(String.valueOf(order.getId()))
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
        csv.append("OrderId,OrderNumber,BuyerId,SellerId,Subtotal,Tax,GrandTotal,PaymentStatus,OrderStatus,ShippingAddress,CreatedAt\n");

        for (Order o : orders) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    o.getId(),
                    o.getOrderNumber(),
                    o.getBuyerId(),
                    o.getSellerId(),
                    o.getSubtotal(),
                    o.getTax(),
                    o.getGrandTotal(),
                    o.getPaymentStatus() != null ? o.getPaymentStatus() : "PENDING",
                    o.getStatus().name(),
                    o.getShippingAddress() != null ? o.getShippingAddress().replace("\"", "\"\"") : "",
                    o.getCreatedAt() != null ? o.getCreatedAt().toString() : ""));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private OrderResponseDto mapToDto(Order order) {
        List<OrderItemDto> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(i -> OrderItemDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        List<OrderTrackingResponseDto> tracking = orderTrackingRepository.findByOrderIdOrderByCreatedAtDesc(String.valueOf(order.getId())).stream()
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
                .sellerId(order.getSellerId())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .grandTotal(order.getGrandTotal())
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "PENDING")
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
