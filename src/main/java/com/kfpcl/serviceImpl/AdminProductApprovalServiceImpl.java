package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductApprovalActionDto;
import com.kfpcl.dto.ProductApprovalResponseDto;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Subcategory;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.SubcategoryRepository;
import com.kfpcl.service.AdminProductApprovalService;
import com.kfpcl.service.AuditLogService;
import com.kfpcl.util.ImageUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProductApprovalServiceImpl implements AdminProductApprovalService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final AuditLogService auditLogService;
    private final ImageUtils imageUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductApprovalResponseDto> getProductApprovals(String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(status)) {
                try {
                    Product.ApprovalStatus appStatus = Product.ApprovalStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("approvalStatus"), appStatus));
                } catch (IllegalArgumentException ignored) {}
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductApprovalResponseDto> dtoList = productPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(productPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductApprovalResponseDto getApprovalById(String approvalId) {
        Product product = productRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductApproval", "approvalId", approvalId));
        return mapToDto(product);
    }

    @Override
    public ProductApprovalResponseDto approveProduct(String approvalId, ProductApprovalActionDto dto) {
        Product product = productRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductApproval", "approvalId", approvalId));

        if (product.getApprovalStatus() == Product.ApprovalStatus.APPROVED) {
            throw new BusinessValidationException("Product is already approved");
        }

        String oldStatus = product.getApprovalStatus().name();
        product.setApprovalStatus(Product.ApprovalStatus.APPROVED);
        product.setRejectionReason(null);
        product.setStatus(Product.Status.ACTIVE);
        Product saved = productRepository.save(product);

        auditLogService.logAction("admin", "ROLE_ADMIN", "APPROVE_PRODUCT", "PRODUCT", approvalId, oldStatus, "APPROVED", null, null);

        return mapToDto(saved);
    }

    @Override
    public ProductApprovalResponseDto rejectProduct(String approvalId, ProductApprovalActionDto dto) {
        Product product = productRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductApproval", "approvalId", approvalId));

        if (product.getApprovalStatus() == Product.ApprovalStatus.APPROVED) {
            throw new BusinessValidationException("Cannot reject an already approved product");
        }

        String reason = (dto != null && StringUtils.hasText(dto.getReason())) ? dto.getReason().trim() : "Product details or compliance standards not met";
        String oldStatus = product.getApprovalStatus().name();
        product.setApprovalStatus(Product.ApprovalStatus.REJECTED);
        product.setRejectionReason(reason);
        product.setStatus(Product.Status.INACTIVE);
        Product saved = productRepository.save(product);

        auditLogService.logAction("admin", "ROLE_ADMIN", "REJECT_PRODUCT", "PRODUCT", approvalId, oldStatus, "REJECTED", null, null);

        return mapToDto(saved);
    }

    private ProductApprovalResponseDto mapToDto(Product product) {
        String catName = categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse(null);
        String subName = subcategoryRepository.findById(product.getSubcategoryId()).map(Subcategory::getName).orElse(null);

        return ProductApprovalResponseDto.builder()
                .approvalId(product.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .categoryId(product.getCategoryId())
                .categoryName(catName)
                .subcategoryId(product.getSubcategoryId())
                .subcategoryName(subName)
                .brand(product.getBrand())
                .description(product.getDescription())
                .imageUrl(imageUtils.generatePresignedUrl(product.getImageUrl()))
                .price(product.getPrice())
                .mrp(product.getMrp())
                .sku(product.getSku())
                .status(product.getStatus().name())
                .approvalStatus(product.getApprovalStatus() != null ? product.getApprovalStatus().name() : Product.ApprovalStatus.APPROVED.name())
                .rejectionReason(product.getRejectionReason())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
