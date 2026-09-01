package com.kfpcl.serviceImpl;

import com.kfpcl.dto.BannerDto;
import com.kfpcl.entity.Banner;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BannerRepository;
import com.kfpcl.service.BannerService;
import com.kfpcl.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final ImageUtils imageUtils;

    @Override
    @Transactional(readOnly = true)
    public List<BannerDto> getAllBanners() {
        return bannerRepository.findByStatusNotOrderByDisplayOrderAscCreatedAtDesc(Banner.Status.ARCHIVED)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerDto> getActiveBanners() {
        return bannerRepository.findByStatusOrderByDisplayOrderAscCreatedAtDesc(Banner.Status.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BannerDto getBannerById(String bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", "bannerId", bannerId));
        return mapToDto(banner);
    }

    @Override
    public BannerDto createBanner(BannerDto dto) {
        String bannerId = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : "banner_" + UUID.randomUUID().toString().substring(0, 8);

        Banner.Status status = Banner.Status.ACTIVE;
        if (dto.getIsActive() != null && !dto.getIsActive()) {
            status = Banner.Status.INACTIVE;
        }
        if (StringUtils.hasText(dto.getStatus())) {
            status = parseStatus(dto.getStatus());
        }

        Banner banner = Banner.builder()
                .id(bannerId)
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .imageUrl(imageUtils.processBase64Image(dto.getImageUrl()))
                .linkUrl(dto.getLinkUrl())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 1)
                .status(status)
                .build();

        Banner saved = bannerRepository.save(banner);
        return mapToDto(saved);
    }

    @Override
    public BannerDto updateBanner(String bannerId, BannerDto dto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseGet(() -> Banner.builder().id(bannerId).build());

        if (dto.getTitle() != null) {
            banner.setTitle(dto.getTitle());
        }
        if (dto.getSubtitle() != null) {
            banner.setSubtitle(dto.getSubtitle());
        }
        if (dto.getImageUrl() != null) {
            banner.setImageUrl(imageUtils.processBase64Image(dto.getImageUrl()));
        }
        if (dto.getLinkUrl() != null) {
            banner.setLinkUrl(dto.getLinkUrl());
        }
        if (dto.getDisplayOrder() != null) {
            banner.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getIsActive() != null) {
            banner.setStatus(dto.getIsActive() ? Banner.Status.ACTIVE : Banner.Status.INACTIVE);
        }
        if (StringUtils.hasText(dto.getStatus())) {
            banner.setStatus(parseStatus(dto.getStatus()));
        }

        Banner saved = bannerRepository.save(banner);
        return mapToDto(saved);
    }

    @Override
    public BannerDto updateBannerStatus(String bannerId, String status, Boolean active) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", "bannerId", bannerId));

        if (active != null) {
            banner.setStatus(active ? Banner.Status.ACTIVE : Banner.Status.INACTIVE);
        } else if (StringUtils.hasText(status)) {
            banner.setStatus(parseStatus(status));
        }

        Banner saved = bannerRepository.save(banner);
        return mapToDto(saved);
    }

    @Override
    public void deleteBanner(String bannerId) {
        bannerRepository.findById(bannerId).ifPresent(bannerRepository::delete);
    }

    private Banner.Status parseStatus(String statusStr) {
        try {
            return Banner.Status.valueOf(statusStr.toUpperCase().trim());
        } catch (Exception e) {
            return Banner.Status.ACTIVE;
        }
    }

    private BannerDto mapToDto(Banner banner) {
        return BannerDto.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(imageUtils.generatePresignedUrl(banner.getImageUrl()))
                .linkUrl(banner.getLinkUrl())
                .displayOrder(banner.getDisplayOrder())
                .status(banner.getStatus() != null ? banner.getStatus().name() : Banner.Status.ACTIVE.name())
                .isActive(banner.isActive())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}
