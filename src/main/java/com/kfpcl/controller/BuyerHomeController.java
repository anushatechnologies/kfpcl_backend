package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.BuyerHomeResponse;
import com.kfpcl.service.BuyerHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyer/home")
@RequiredArgsConstructor
public class BuyerHomeController {

    private final BuyerHomeService buyerHomeService;

    @GetMapping
    public ResponseEntity<ApiResponse<BuyerHomeResponse>> getBuyerHome() {
        BuyerHomeResponse response = buyerHomeService.getBuyerHomeDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
