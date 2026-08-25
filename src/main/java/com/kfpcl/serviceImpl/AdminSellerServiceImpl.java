package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.SellerApplication;
import com.kfpcl.entity.User;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.SellerApplicationRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.AdminSellerService;
import com.kfpcl.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSellerServiceImpl implements AdminSellerService {

    private final UserRepository userRepository;
    private final SellerApplicationRepository sellerApplicationRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminUserResponseDto> getSellers(String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> sellerPage = userRepository.findByRole(User.Role.SUPPLIER, pageable);
        List<AdminUserResponseDto> dtoList = sellerPage.getContent().stream()
                .map(u -> AdminUserResponseDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .role(u.getRole().name())
                        .status(u.getStatus().name())
                        .region(u.getRegion())
                        .createdAt(u.getCreatedAt())
                        .updatedAt(u.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return PageResponseDto.from(sellerPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SellerApplicationResponseDto> getApplications(String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SellerApplication> appPage;
        if (StringUtils.hasText(status)) {
            try {
                SellerApplication.Status sStatus = SellerApplication.Status.valueOf(status.trim().toUpperCase());
                appPage = sellerApplicationRepository.findByStatus(sStatus, pageable);
            } catch (IllegalArgumentException e) {
                appPage = sellerApplicationRepository.findAll(pageable);
            }
        } else {
            appPage = sellerApplicationRepository.findAll(pageable);
        }

        List<SellerApplicationResponseDto> dtoList = appPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(appPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerApplicationResponseDto getApplicationById(String applicationId) {
        SellerApplication application = sellerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerApplication", "applicationId", applicationId));
        return mapToDto(application);
    }

    @Override
    public SellerApplicationResponseDto approveApplication(String applicationId, SellerActionDto dto) {
        SellerApplication application = sellerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerApplication", "applicationId", applicationId));

        if (application.getStatus() == SellerApplication.Status.APPROVED) {
            throw new BusinessValidationException("Seller application is already approved");
        }

        application.setStatus(SellerApplication.Status.APPROVED);
        application.setVerifiedAt(LocalDateTime.now());
        application.setVerifiedBy("admin");
        application.setRejectionReason(null);
        SellerApplication saved = sellerApplicationRepository.save(application);

        // Update user role to SUPPLIER
        userRepository.findById(application.getUserId()).ifPresent(u -> {
            u.setRole(User.Role.SUPPLIER);
            userRepository.save(u);
        });

        auditLogService.logAction("admin", "ROLE_ADMIN", "APPROVE_SELLER_APPLICATION", "SELLER_APPLICATION", applicationId, "PENDING", "APPROVED", null, null);

        return mapToDto(saved);
    }

    @Override
    public SellerApplicationResponseDto rejectApplication(String applicationId, SellerActionDto dto) {
        SellerApplication application = sellerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerApplication", "applicationId", applicationId));

        if (application.getStatus() == SellerApplication.Status.APPROVED) {
            throw new BusinessValidationException("Cannot reject an already approved application");
        }

        application.setStatus(SellerApplication.Status.REJECTED);
        application.setRejectionReason(dto != null && StringUtils.hasText(dto.getReason()) ? dto.getReason().trim() : "Documents incomplete or invalid");
        application.setVerifiedAt(LocalDateTime.now());
        application.setVerifiedBy("admin");
        SellerApplication saved = sellerApplicationRepository.save(application);

        auditLogService.logAction("admin", "ROLE_ADMIN", "REJECT_SELLER_APPLICATION", "SELLER_APPLICATION", applicationId, "PENDING", "REJECTED", null, null);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SellerStoreResponseDto> getSellerStores(String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> sellers = userRepository.findByRole(User.Role.SUPPLIER, pageable);
        List<SellerStoreResponseDto> stores = sellers.getContent().stream()
                .map(s -> {
                    SellerApplication app = sellerApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(s.getId()).orElse(null);
                    return SellerStoreResponseDto.builder()
                            .sellerId(s.getId())
                            .businessName(app != null ? app.getBusinessName() : s.getName() + " Store")
                            .ownerName(s.getName())
                            .email(s.getEmail())
                            .phone(s.getPhone())
                            .gstin(app != null ? app.getGstin() : "N/A")
                            .status(s.getStatus().name())
                            .totalProducts(12)
                            .totalOrders(45L)
                            .totalRevenue(150000.0)
                            .joinedAt(s.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponseDto.from(sellers, stores);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerApplicationResponseDto getSellerVerification(String sellerId) {
        SellerApplication app = sellerApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerVerification", "sellerId", sellerId));
        return mapToDto(app);
    }

    private SellerApplicationResponseDto mapToDto(SellerApplication app) {
        User user = userRepository.findById(app.getUserId()).orElse(null);
        return SellerApplicationResponseDto.builder()
                .id(app.getId())
                .userId(app.getUserId())
                .userName(user != null ? user.getName() : "Unknown")
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .businessName(app.getBusinessName())
                .businessType(app.getBusinessType())
                .gstin(app.getGstin())
                .panNumber(app.getPanNumber())
                .address(app.getAddress())
                .documents(app.getDocuments())
                .status(app.getStatus().name())
                .rejectionReason(app.getRejectionReason())
                .verifiedAt(app.getVerifiedAt())
                .verifiedBy(app.getVerifiedBy())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
