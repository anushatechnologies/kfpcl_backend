package com.kfpcl.service;

import com.kfpcl.dto.BrandCreateDto;
import com.kfpcl.dto.BrandResponseDto;
import com.kfpcl.dto.BrandUpdateDto;
import com.kfpcl.dto.PageResponseDto;

public interface BrandService {

    PageResponseDto<BrandResponseDto> getBrands(String search, String status, int page, int size, String sortBy, String sortDir);

    BrandResponseDto getBrandById(String brandId);

    BrandResponseDto createBrand(BrandCreateDto dto);

    BrandResponseDto updateBrand(String brandId, BrandUpdateDto dto);

    void deleteBrand(String brandId);
}
