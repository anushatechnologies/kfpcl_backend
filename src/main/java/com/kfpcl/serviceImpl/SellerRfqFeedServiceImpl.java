package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.SellerRfqFeedResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.RFQStatus;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.service.SellerProfileService;
import com.kfpcl.service.SellerRfqFeedService;
import com.kfpcl.specification.RfqSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerRfqFeedServiceImpl implements SellerRfqFeedService {

    private final RfqRepository rfqRepository;
    private final SellerProfileService sellerProfileService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerRfqFeedResponse> getOpenRfqFeed(
            String sellerEmail,
            Long categoryId,
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Validate seller exists
        Seller seller = sellerProfileService.getSellerEntityByEmail(sellerEmail);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Rfq> spec = RfqSpecification.filterOpenRfqs(categoryId, keyword);

        Page<Rfq> rfqPage = rfqRepository.findAll(spec, pageable);

        List<SellerRfqFeedResponse> mapped = rfqPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(rfqPage, mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerRfqFeedResponse getRfqFeedDetails(String sellerEmail, Long rfqId) {
        sellerProfileService.getSellerEntityByEmail(sellerEmail);

        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        if (rfq.getStatus() != RFQStatus.OPEN) {
            throw new ResourceNotFoundException("Active RFQ opportunity not found with id: " + rfqId);
        }

        return mapToResponse(rfq);
    }

    private SellerRfqFeedResponse mapToResponse(Rfq rfq) {
        if (rfq == null) {
            return null;
        }

        Category category = rfq.getCategory();
        Buyer buyer = rfq.getBuyer();

        return SellerRfqFeedResponse.builder()
                .id(rfq.getId())
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
                .buyerBusinessCity(buyer != null ? buyer.getCity() : null)
                .createdAt(rfq.getCreatedAt())
                .build();
    }
}
