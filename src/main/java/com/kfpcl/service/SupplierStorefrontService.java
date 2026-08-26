package com.kfpcl.service;

import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.dto.response.SupplierStorefrontResponse;

public interface SupplierStorefrontService {

    /**
     * Retrieves public supplier storefront details with verified info and featured products.
     */
    SupplierStorefrontResponse getStorefrontById(Long supplierId);

    /**
     * Retrieves paginated list of active and approved products published by this supplier.
     */
    PageResponse<ProductResponse> getSupplierProducts(
            Long supplierId,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
