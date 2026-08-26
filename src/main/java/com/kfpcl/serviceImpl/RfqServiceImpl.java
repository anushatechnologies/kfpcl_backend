package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.RfqCreateRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.RfqResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.enums.RFQStatus;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.service.RfqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfqServiceImpl implements RfqService {

    private final RfqRepository rfqRepository;
    private final BuyerRepository buyerRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public RfqResponse createRfq(String buyerEmail, RfqCreateRequest request) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (Boolean.FALSE.equals(category.getIsActive())) {
            throw new IllegalArgumentException("Selected category is inactive");
        }

        Rfq rfq = Rfq.builder()
                .buyer(buyer)
                .category(category)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .quantity(request.getQuantity())
                .unit(request.getUnit().trim().toUpperCase())
                .targetUnitPrice(request.getTargetUnitPrice())
                .deliveryLocation(request.getDeliveryLocation().trim())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms().trim() : null)
                .status(RFQStatus.OPEN)
                .specifications(request.getSpecifications() != null ? request.getSpecifications() : new HashMap<>())
                .build();

        Rfq savedRfq = rfqRepository.save(rfq);
        return mapToResponse(savedRfq);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RfqResponse> getBuyerRfqs(String buyerEmail, RFQStatus status, int page, int size) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Rfq> rfqPage = (status != null)
                ? rfqRepository.findByBuyerIdAndStatus(buyer.getId(), status, pageable)
                : rfqRepository.findByBuyerId(buyer.getId(), pageable);

        List<RfqResponse> mapped = rfqPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(rfqPage, mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public RfqResponse getBuyerRfqById(String buyerEmail, Long rfqId) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        return mapToResponse(rfq);
    }

    @Override
    @Transactional
    public RfqResponse cancelRfq(String buyerEmail, Long rfqId) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        if (rfq.getStatus() != RFQStatus.OPEN) {
            throw new IllegalStateException("Only OPEN RFQs can be cancelled. Current status: " + rfq.getStatus());
        }

        rfq.setStatus(RFQStatus.CANCELLED);
        Rfq updated = rfqRepository.save(rfq);
        return mapToResponse(updated);
    }

    private RfqResponse mapToResponse(Rfq rfq) {
        if (rfq == null) {
            return null;
        }

        Buyer buyer = rfq.getBuyer();
        Category category = rfq.getCategory();

        return RfqResponse.builder()
                .id(rfq.getId())
                .buyerId(buyer != null ? buyer.getId() : null)
                .buyerCompanyName(buyer != null ? buyer.getCompanyName() : null)
                .buyerContactPerson(buyer != null ? buyer.getContactPerson() : null)
                .buyerEmail(buyer != null && buyer.getUser() != null ? buyer.getUser().getEmail() : null)
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categorySlug(category != null ? category.getSlug() : null)
                .title(rfq.getTitle())
                .description(rfq.getDescription())
                .quantity(rfq.getQuantity())
                .unit(rfq.getUnit())
                .targetUnitPrice(rfq.getTargetUnitPrice())
                .deliveryLocation(rfq.getDeliveryLocation())
                .expectedDeliveryDate(rfq.getExpectedDeliveryDate())
                .paymentTerms(rfq.getPaymentTerms())
                .status(rfq.getStatus())
                .specifications(rfq.getSpecifications())
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }
}
