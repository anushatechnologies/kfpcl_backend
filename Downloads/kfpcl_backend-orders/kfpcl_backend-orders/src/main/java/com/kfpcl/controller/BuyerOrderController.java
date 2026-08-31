package com.kfpcl.controller;

import com.kfpcl.dto.OrderCreateDto;
import com.kfpcl.dto.OrderResponseDto;
import com.kfpcl.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "buyer_1") String buyerId) {
        OrderResponseDto response = orderService.createOrder(orderCreateDto, buyerId, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getBuyerOrders(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "buyer_1") String buyerId) {
        List<OrderResponseDto> orders = orderService.getBuyerOrders(buyerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getBuyerOrderById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "buyer_1") String buyerId) {
        OrderResponseDto order = orderService.getBuyerOrderById(id, buyerId);
        return ResponseEntity.ok(order);
    }
}

