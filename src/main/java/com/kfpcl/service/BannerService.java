package com.kfpcl.service;

import com.kfpcl.dto.BannerDto;

import java.util.List;

public interface BannerService {

    List<BannerDto> getAllBanners();

    List<BannerDto> getActiveBanners();

    BannerDto getBannerById(String bannerId);

    BannerDto createBanner(BannerDto dto);

    BannerDto updateBanner(String bannerId, BannerDto dto);

    BannerDto updateBannerStatus(String bannerId, String status, Boolean active);

    void deleteBanner(String bannerId);
}
