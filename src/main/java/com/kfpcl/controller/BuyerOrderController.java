package com.kfpcl.controller;

import com.kfpcl.dto.OrderCreateDto;
import com.kfpcl.dto.OrderResponseDto;
import com.kfpcl.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/orders")
public class BuyerOrderController {

    private final OrderService orderService;

    public BuyerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateDto orderCreateDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication) {
        String buyerId = authentication != null ? authentication.getName() : "buyer_1";
        OrderResponseDto response = orderService.createOrder(orderCreateDto, buyerId, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getBuyerOrders(Authentication authentication) {
        String buyerId = authentication != null ? authentication.getName() : "buyer_1";
        List<OrderResponseDto> orders = orderService.getBuyerOrders(buyerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getBuyerOrderById(
            @PathVariable("id") Long id,
            Authentication authentication) {
        String buyerId = authentication != null ? authentication.getName() : "buyer_1";
        OrderResponseDto order = orderService.getBuyerOrderById(id, buyerId);
        return ResponseEntity.ok(order);
    }
}
