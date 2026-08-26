package com.kfpcl.service;

import com.kfpcl.dto.request.ProductCreateRequest;
import com.kfpcl.dto.request.ProductUpdateRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.entity.enums.ProductStatus;

public interface SellerProductService {

    /**
     * Creates a new product for the authenticated seller.
     */
    ProductResponse createProduct(String userEmail, ProductCreateRequest request);

    /**
     * Retrieves paginated products belonging to the authenticated seller.
     */
    PageResponse<ProductResponse> getSellerProducts(
            String userEmail,
            ProductStatus status,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    /**
     * Retrieves specific product details belonging to the authenticated seller.
     */
    ProductResponse getSellerProductById(String userEmail, Long productId);

    /**
     * Updates an existing product for the authenticated seller.
     */
    ProductResponse updateProduct(String userEmail, Long productId, ProductUpdateRequest request);

    /**
     * Deactivates or archives a product belonging to the authenticated seller.
     */
    void deleteProduct(String userEmail, Long productId);
}
