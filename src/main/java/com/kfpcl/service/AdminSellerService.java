package com.kfpcl.service;

import com.kfpcl.dto.*;

public interface AdminSellerService {

    PageResponseDto<AdminUserResponseDto> getSellers(String search, String status, int page, int size, String sortBy, String sortDir);

    PageResponseDto<SellerApplicationResponseDto> getApplications(String status, int page, int size, String sortBy, String sortDir);

    SellerApplicationResponseDto getApplicationById(String applicationId);

    SellerApplicationResponseDto approveApplication(String applicationId, SellerActionDto dto);

    SellerApplicationResponseDto rejectApplication(String applicationId, SellerActionDto dto);

    PageResponseDto<SellerStoreResponseDto> getSellerStores(String search, int page, int size, String sortBy, String sortDir);

    SellerApplicationResponseDto getSellerVerification(String sellerId);
}
