package com.kfpcl.serviceImpl;

import com.kfpcl.dto.CreateRfqRequest;
import com.kfpcl.dto.RfqResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Rfq;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.exception.UnprocessableEntityException;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.RfqService;
import com.kfpcl.service.SupplierNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfqServiceImpl implements RfqService {

    private final RfqRepository rfqRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final QuotationRepository quotationRepository;
    private final SupplierNotificationService supplierNotificationService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public RfqResponse createRfq(CreateRfqRequest request) {
        Buyer buyer = securityUtils.getCurrentBuyer();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        String productTitle = request.getProductTitle() != null && !request.getProductTitle().isBlank()
                ? request.getProductTitle()
                : product.getTitle();

        Rfq rfq = Rfq.builder()
                .id("rfq_" + UUID.randomUUID().toString().substring(0, 8))
                .buyer(buyer)
                .product(product)
                .productTitle(productTitle)
                .category(category)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .targetPrice(request.getTargetPrice())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .description(request.getDescription())
                .status(Rfq.Status.OPEN)
                .build();

        Rfq saved = rfqRepository.save(rfq);

        // Notify eligible suppliers
        supplierNotificationService.notifyEligibleSuppliers(saved);

        return mapToRfqResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RfqResponse> getBuyerRfqs() {
        Buyer buyer = securityUtils.getCurrentBuyer();
        return rfqRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(this::mapToRfqResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RfqResponse getBuyerRfqById(String rfqId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        return mapToRfqResponse(rfq);
    }

    @Override
    @Transactional
    public RfqResponse cancelRfq(String rfqId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        if (rfq.getStatus() != Rfq.Status.OPEN && rfq.getStatus() != Rfq.Status.QUOTATIONS_RECEIVED) {
            throw new UnprocessableEntityException("Cannot cancel RFQ in status: " + rfq.getStatus());
        }

        rfq.setStatus(Rfq.Status.CANCELLED);
        Rfq updated = rfqRepository.save(rfq);
        return mapToRfqResponse(updated);
    }

    private RfqResponse mapToRfqResponse(Rfq rfq) {
        int quotationsCount = quotationRepository.findByRfqIdOrderByQuotedPriceAsc(rfq.getId()).size();

        return RfqResponse.builder()
                .id(rfq.getId())
                .buyerId(rfq.getBuyer() != null ? rfq.getBuyer().getId() : null)
                .productId(rfq.getProduct() != null ? rfq.getProduct().getId() : null)
                .productTitle(rfq.getProductTitle())
                .categoryId(rfq.getCategory() != null ? rfq.getCategory().getId() : null)
                .categoryName(rfq.getCategory() != null ? rfq.getCategory().getName() : null)
                .quantity(rfq.getQuantity())
                .unit(rfq.getUnit())
                .targetPrice(rfq.getTargetPrice())
                .expectedDeliveryDate(rfq.getExpectedDeliveryDate())
                .description(rfq.getDescription())
                .status(rfq.getStatus().name())
                .quotationsCount(quotationsCount)
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }
}
