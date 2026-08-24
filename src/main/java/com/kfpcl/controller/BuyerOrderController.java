package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.OrderTrackingResponse;
import com.kfpcl.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/orders")
@RequiredArgsConstructor
public class BuyerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BuyerOrderResponse>>> getOrders(
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String status
    ) {
        List<BuyerOrderResponse> orders = orderService.getBuyerOrders(status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<BuyerOrderResponse>> getOrderById(
            @PathVariable("orderId") String orderId
    ) {
        BuyerOrderResponse order = orderService.getBuyerOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<BuyerOrderResponse>> cancelOrder(
            @PathVariable("orderId") String orderId
    ) {
        BuyerOrderResponse order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderId}/confirm-delivery")
    public ResponseEntity<ApiResponse<BuyerOrderResponse>> confirmDelivery(
            @PathVariable("orderId") String orderId
    ) {
        BuyerOrderResponse order = orderService.confirmDelivery(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> getOrderTracking(
            @PathVariable("orderId") String orderId
    ) {
        OrderTrackingResponse tracking = orderService.getOrderTracking(orderId);
        return ResponseEntity.ok(ApiResponse.success(tracking));
    }
}
