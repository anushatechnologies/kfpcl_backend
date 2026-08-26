package com.kfpcl.controller;

import com.kfpcl.dto.OrderResponseDto;
import com.kfpcl.dto.OrderStatusUpdateDto;
import com.kfpcl.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;

    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getSellerOrders(Authentication authentication) {
        String sellerId = authentication != null ? authentication.getName() : "seller_1";
        List<OrderResponseDto> orders = orderService.getSellerOrders(sellerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody OrderStatusUpdateDto updateDto,
            Authentication authentication) {
        String sellerId = authentication != null ? authentication.getName() : "seller_1";
        OrderResponseDto updated = orderService.updateOrderStatus(id, updateDto, sellerId);
        return ResponseEntity.ok(updated);
    }
}
