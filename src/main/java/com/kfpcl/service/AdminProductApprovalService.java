package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductApprovalActionDto;
import com.kfpcl.dto.ProductApprovalResponseDto;

public interface AdminProductApprovalService {

    PageResponseDto<ProductApprovalResponseDto> getProductApprovals(String status, int page, int size, String sortBy, String sortDir);

    ProductApprovalResponseDto getApprovalById(String approvalId);

    ProductApprovalResponseDto approveProduct(String approvalId, ProductApprovalActionDto dto);

    ProductApprovalResponseDto rejectProduct(String approvalId, ProductApprovalActionDto dto);
}
