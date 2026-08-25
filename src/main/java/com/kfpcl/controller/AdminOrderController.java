package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<OrderResponseDto>>> listOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<OrderResponseDto> orders = adminOrderService.getOrders(search, status, paymentStatus, region, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(@PathVariable String orderId) {
        OrderResponseDto order = adminOrderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order details retrieved successfully"));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderStatusUpdateDto dto) {

        OrderResponseDto updated = adminOrderService.updateOrderStatus(orderId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Order status updated successfully"));
    }

    @PostMapping("/{orderId}/tracking")
    public ResponseEntity<ApiResponse<OrderTrackingResponseDto>> addOrderTracking(
            @PathVariable String orderId,
            @Valid @RequestBody OrderTrackingCreateDto dto) {

        OrderTrackingResponseDto tracking = adminOrderService.addOrderTracking(orderId, dto);
        return ResponseEntity.ok(ApiResponse.success(tracking, "Order tracking information added successfully"));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportOrders() {
        byte[] csvData = adminOrderService.exportOrdersCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=kfpcl_orders.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
