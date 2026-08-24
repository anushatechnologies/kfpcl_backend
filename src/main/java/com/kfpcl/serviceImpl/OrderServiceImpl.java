package com.kfpcl.serviceImpl;

import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.OrderItemResponse;
import com.kfpcl.dto.OrderTrackingResponse;
import com.kfpcl.dto.SupplierSummaryDto;
import com.kfpcl.entity.*;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.exception.UnprocessableEntityException;
import com.kfpcl.repository.OrderItemRepository;
import com.kfpcl.repository.OrderRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public Order createOrderFromQuotation(Quotation quotation, String shippingAddress) {
        Rfq rfq = quotation.getRfq();
        Buyer buyer = rfq.getBuyer();
        Supplier supplier = quotation.getSupplier();

        BigDecimal unitPrice = quotation.getQuotedPrice();
        Integer quantity = quotation.getQuantity();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);
        String trackingNumber = "TRK" + System.currentTimeMillis();

        LocalDate estDelivery = quotation.getLeadTimeDays() != null
                ? LocalDate.now().plusDays(quotation.getLeadTimeDays())
                : rfq.getExpectedDeliveryDate();

        String finalShippingAddress = shippingAddress != null && !shippingAddress.isBlank()
                ? shippingAddress
                : (buyer.getAddress() != null ? buyer.getAddress() : "Buyer Registered Address");

        Order order = Order.builder()
                .id(orderId)
                .buyer(buyer)
                .supplier(supplier)
                .rfq(rfq)
                .quotation(quotation)
                .totalAmount(totalAmount)
                .status(Order.Status.PLACED)
                .shippingAddress(finalShippingAddress)
                .trackingNumber(trackingNumber)
                .courierPartner("BlueDart Logistics")
                .estimatedDelivery(estDelivery)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .product(rfq.getProduct())
                .productTitle(rfq.getProductTitle())
                .quantity(quantity)
                .unit(rfq.getUnit())
                .unitPrice(unitPrice)
                .totalPrice(totalAmount)
                .build();

        order.getItems().add(item);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuyerOrderResponse> getBuyerOrders(String status) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        List<Order> orders;

        if (status != null && !status.equalsIgnoreCase("ALL") && !status.isBlank()) {
            try {
                Order.Status orderStatus = Order.Status.valueOf(status.toUpperCase());
                orders = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyer.getId(), orderStatus);
            } catch (IllegalArgumentException ex) {
                throw new UnprocessableEntityException("Invalid order status: " + status);
            }
        } else {
            orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId());
        }

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerOrderResponse getBuyerOrderById(String orderId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public BuyerOrderResponse cancelOrder(String orderId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != Order.Status.PLACED && order.getStatus() != Order.Status.CONFIRMED) {
            throw new UnprocessableEntityException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(Order.Status.CANCELLED);
        Order updated = orderRepository.save(order);
        return mapToOrderResponse(updated);
    }

    @Override
    @Transactional
    public BuyerOrderResponse confirmDelivery(String orderId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != Order.Status.SHIPPED) {
            throw new UnprocessableEntityException("Delivery can only be confirmed for SHIPPED orders. Current status: " + order.getStatus());
        }

        order.setStatus(Order.Status.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);
        return mapToOrderResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(String orderId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return OrderTrackingResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .courierPartner(order.getCourierPartner())
                .trackingNumber(order.getTrackingNumber())
                .shippingAddress(order.getShippingAddress())
                .estimatedDelivery(order.getEstimatedDelivery())
                .deliveredAt(order.getDeliveredAt())
                .orderDate(order.getCreatedAt())
                .build();
    }

    private BuyerOrderResponse mapToOrderResponse(Order order) {
        SupplierSummaryDto supplierDto = null;
        if (order.getSupplier() != null) {
            supplierDto = SupplierSummaryDto.builder()
                    .id(order.getSupplier().getId())
                    .companyName(order.getSupplier().getCompanyName())
                    .gstVerified(order.getSupplier().getGstVerified())
                    .isVerified(order.getSupplier().getIsVerified())
                    .build();
        }

        List<OrderItemResponse> itemDtos = order.getItems() != null
                ? order.getItems().stream().map(item -> OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productTitle(item.getProductTitle())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return BuyerOrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyer() != null ? order.getBuyer().getId() : null)
                .supplier(supplierDto)
                .rfqId(order.getRfq() != null ? order.getRfq().getId() : null)
                .quotationId(order.getQuotation() != null ? order.getQuotation().getId() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .trackingNumber(order.getTrackingNumber())
                .courierPartner(order.getCourierPartner())
                .estimatedDelivery(order.getEstimatedDelivery())
                .deliveredAt(order.getDeliveredAt())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
