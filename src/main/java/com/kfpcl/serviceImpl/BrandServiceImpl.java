package com.kfpcl.serviceImpl;

import com.kfpcl.dto.BrandCreateDto;
import com.kfpcl.dto.BrandResponseDto;
import com.kfpcl.dto.BrandUpdateDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.Brand;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BrandRepository;
import com.kfpcl.service.AuditLogService;
import com.kfpcl.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BrandResponseDto> getBrands(String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Brand> brandPage;
        if (StringUtils.hasText(search) && StringUtils.hasText(status)) {
            Brand.Status bStatus = parseStatus(status);
            brandPage = brandRepository.findByNameContainingIgnoreCaseAndStatus(search.trim(), bStatus, pageable);
        } else if (StringUtils.hasText(search)) {
            brandPage = brandRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else if (StringUtils.hasText(status)) {
            Brand.Status bStatus = parseStatus(status);
            brandPage = brandRepository.findByStatus(bStatus, pageable);
        } else {
            brandPage = brandRepository.findAll(pageable);
        }

        List<BrandResponseDto> dtoList = brandPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(brandPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(String brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "brandId", brandId));
        return mapToDto(brand);
    }

    @Override
    public BrandResponseDto createBrand(BrandCreateDto dto) {
        String name = dto.getName().trim();
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Brand", "name", name);
        }

        String brandId = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : "brand_" + slugify(name);

        if (brandRepository.existsById(brandId)) {
            brandId = brandId + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        Brand.Status status = StringUtils.hasText(dto.getStatus()) ? parseStatus(dto.getStatus()) : Brand.Status.ACTIVE;

        Brand brand = Brand.builder()
                .id(brandId)
                .name(name)
                .slug(slugify(name))
                .logoUrl(dto.getLogoUrl())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .status(status)
                .build();

        Brand saved = brandRepository.save(brand);
        auditLogService.logAction("admin", "ROLE_ADMIN", "CREATE_BRAND", "BRAND", saved.getId(), null, saved.getName(), null, null);

        return mapToDto(saved);
    }

    @Override
    public BrandResponseDto updateBrand(String brandId, BrandUpdateDto dto) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "brandId", brandId));

        if (StringUtils.hasText(dto.getName())) {
            String newName = dto.getName().trim();
            if (brandRepository.existsByNameIgnoreCaseAndIdNot(newName, brandId)) {
                throw new DuplicateResourceException("Brand", "name", newName);
            }
            brand.setName(newName);
            brand.setSlug(slugify(newName));
        }

        if (dto.getLogoUrl() != null) {
            brand.setLogoUrl(dto.getLogoUrl());
        }
        if (dto.getDescription() != null) {
            brand.setDescription(dto.getDescription());
        }
        if (dto.getWebsite() != null) {
            brand.setWebsite(dto.getWebsite());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            brand.setStatus(parseStatus(dto.getStatus()));
        }

        Brand updated = brandRepository.save(brand);
        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_BRAND", "BRAND", brandId, null, updated.getName(), null, null);

        return mapToDto(updated);
    }

    @Override
    public void deleteBrand(String brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "brandId", brandId));

        brand.setStatus(Brand.Status.ARCHIVED);
        brandRepository.save(brand);
        auditLogService.logAction("admin", "ROLE_ADMIN", "ARCHIVE_BRAND", "BRAND", brandId, null, "ARCHIVED", null, null);
    }

    private Brand.Status parseStatus(String status) {
        try {
            return Brand.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Brand.Status.ACTIVE;
        }
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private BrandResponseDto mapToDto(Brand brand) {
        return BrandResponseDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .logoUrl(brand.getLogoUrl())
                .description(brand.getDescription())
                .website(brand.getWebsite())
                .status(brand.getStatus().name())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}
