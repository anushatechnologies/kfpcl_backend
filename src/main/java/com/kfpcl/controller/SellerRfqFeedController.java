package com.kfpcl.controller;

import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.SellerRfqFeedResponse;
import com.kfpcl.service.SellerRfqFeedService;
import com.kfpcl.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/seller/rfq/feed")
@RequiredArgsConstructor
public class SellerRfqFeedController {

    private final SellerRfqFeedService sellerRfqFeedService;

    /**
     * Protected API: Discover open RFQ opportunities across the marketplace.
     * GET /api/v1/seller/rfq/feed
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SellerRfqFeedResponse>>> getOpenRfqFeed(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<SellerRfqFeedResponse> feed = sellerRfqFeedService.getOpenRfqFeed(
                email, categoryId, keyword, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("Open RFQ opportunities retrieved successfully", feed));
    }

    /**
     * Protected API: View specific RFQ opportunity details.
     * GET /api/v1/seller/rfq/feed/{rfqId}
     */
    @GetMapping("/{rfqId}")
    public ResponseEntity<ApiResponse<SellerRfqFeedResponse>> getRfqFeedDetails(
            @PathVariable Long rfqId,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        SellerRfqFeedResponse details = sellerRfqFeedService.getRfqFeedDetails(email, rfqId);
        return ResponseEntity.ok(ApiResponse.success("RFQ opportunity details retrieved successfully", details));
    }
}
