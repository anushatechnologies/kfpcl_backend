package com.kfpcl.service;

import com.kfpcl.dto.request.ProductFilterRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductResponse;

public interface ProductCatalogService {

    /**
     * Searches, filters, and paginates public products complying with marketplace visibility rules.
     */
    PageResponse<ProductResponse> searchProducts(
            ProductFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    /**
     * Retrieves public product details by ID and increments view count.
     */
    ProductResponse getProductDetailsById(Long id);

    /**
     * Retrieves public product details by slug and increments view count.
     */
    ProductResponse getProductDetailsBySlug(String slug);
}
