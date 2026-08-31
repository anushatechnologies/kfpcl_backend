package com.kfpcl.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.*;
import com.kfpcl.entity.*;
import com.kfpcl.exception.InvalidStateTransitionException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.IdempotencyRecordRepository;
import com.kfpcl.repository.OrderAuditLogRepository;
import com.kfpcl.repository.OrderRepository;
import com.kfpcl.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderAuditLogRepository auditLogRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderAuditLogRepository auditLogRepository,
                            IdempotencyRecordRepository idempotencyRecordRepository,
                            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.auditLogRepository = auditLogRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto, String buyerId, String idempotencyKey) {
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<IdempotencyRecord> recordOpt = idempotencyRecordRepository.findById(idempotencyKey);
            if (recordOpt.isPresent()) {
                try {
                    return objectMapper.readValue(recordOpt.get().getResponseBody(), OrderResponseDto.class);
                } catch (Exception ex) {
                    // fallback
                }
            }
        }

        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemDto : dto.getItems()) {
            BigDecimal lineTotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            calculatedSubtotal = calculatedSubtotal.add(lineTotal);

            OrderItem item = OrderItem.builder()
                    .productId(itemDto.getProductId())
                    .productName(itemDto.getProductName() != null ? itemDto.getProductName() : "Product " + itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .totalPrice(lineTotal)
                    .build();
            orderItems.add(item);
        }

        BigDecimal calculatedTax = calculatedSubtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingCost = dto.getShippingCost() != null ? dto.getShippingCost() : BigDecimal.ZERO;
        BigDecimal calculatedGrandTotal = calculatedSubtotal.add(calculatedTax).add(shippingCost);

        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + (1000 + new Random().nextInt(9000));
        String sellerId = dto.getSellerId() != null ? dto.getSellerId() : "seller_1";

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .status(OrderStatus.CREATED)
                .subtotal(calculatedSubtotal)
                .tax(calculatedTax)
                .shippingCost(shippingCost)
                .grandTotal(calculatedGrandTotal)
                .shippingAddress(dto.getShippingAddress())
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "bank")
                .paymentStatus("PENDING")
                .idempotencyKey(idempotencyKey)
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        OrderAuditLog auditLog = OrderAuditLog.builder()
                .orderId(savedOrder.getId())
                .fromStatus(null)
                .toStatus(OrderStatus.CREATED)
                .changedBy(buyerId)
                .notes("Order placed by buyer")
                .build();
        auditLogRepository.save(auditLog);

        OrderResponseDto responseDto = mapToOrderResponseDto(savedOrder);

        if (StringUtils.hasText(idempotencyKey)) {
            try {
                String responseJson = objectMapper.writeValueAsString(responseDto);
                IdempotencyRecord record = IdempotencyRecord.builder()
                        .idempotencyKey(idempotencyKey)
                        .responseBody(responseJson)
                        .statusCode(200)
                        .createdOrderId(savedOrder.getId())
                        .build();
                idempotencyRecordRepository.save(record);
            } catch (Exception e) {
                // Ignore
            }
        }

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getBuyerOrders(String buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(this::mapToOrderResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getBuyerOrderById(Long orderId, String buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!order.getBuyerId().equals(buyerId)) {
            throw new ResourceNotFoundException("Order not found for buyer: " + buyerId);
        }
        return mapToOrderResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getSellerOrders(String sellerId) {
        return orderRepository.findBySellerId(sellerId).stream()
                .map(this::mapToOrderResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatusUpdateDto updateDto, String sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = updateDto.getStatus();

        validateStateTransition(currentStatus, newStatus);

        order.setStatus(newStatus);
        if (StringUtils.hasText(updateDto.getTrackingNo())) {
            order.setTrackingNo(updateDto.getTrackingNo());
        }
        if (StringUtils.hasText(updateDto.getShippingCarrier())) {
            order.setShippingCarrier(updateDto.getShippingCarrier());
        }

        Order updatedOrder = orderRepository.save(order);

        OrderAuditLog auditLog = OrderAuditLog.builder()
                .orderId(updatedOrder.getId())
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(sellerId)
                .notes("Status updated to " + newStatus + (updateDto.getTrackingNo() != null ? " with tracking: " + updateDto.getTrackingNo() : ""))
                .build();
        auditLogRepository.save(auditLog);

        return mapToOrderResponseDto(updatedOrder);
    }

    private void validateStateTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return;
        }

        boolean isValid = false;
        switch (current) {
            case CREATED:
                isValid = (next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED);
                break;
            case PROCESSING:
                isValid = (next == OrderStatus.PACKED || next == OrderStatus.CANCELLED);
                break;
            case PACKED:
                isValid = (next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED);
                break;
            case SHIPPED:
                isValid = (next == OrderStatus.DELIVERED);
                break;
            case DELIVERED:
            case CANCELLED:
                isValid = false;
                break;
        }

        if (!isValid) {
            throw new InvalidStateTransitionException(
                    "Invalid state transition from " + current + " to " + next
            );
        }
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        List<OrderAuditLogDto> auditDtos = auditLogRepository.findByOrderIdOrderByTimestampAsc(order.getId())
                .stream()
                .map(log -> OrderAuditLogDto.builder()
                        .id(log.getId())
                        .fromStatus(log.getFromStatus())
                        .toStatus(log.getToStatus())
                        .changedBy(log.getChangedBy())
                        .notes(log.getNotes())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .grandTotal(order.getGrandTotal())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .trackingNo(order.getTrackingNo())
                .shippingCarrier(order.getShippingCarrier())
                .idempotencyKey(order.getIdempotencyKey())
                .items(itemDtos)
                .auditTimeline(auditDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
