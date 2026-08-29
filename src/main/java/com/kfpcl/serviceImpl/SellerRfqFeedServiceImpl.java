package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.dto.request.QuotationSubmitRequest;
import com.kfpcl.entity.Quotation;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.User;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.SellerRfqFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerRfqFeedServiceImpl implements SellerRfqFeedService {

    private final RfqRepository rfqRepository;
    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<RfqResponseDto> getOpenRfqs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Rfq> rfqs = rfqRepository.findAll(pageable);
        
        List<Rfq> openRfqs = rfqs.getContent().stream()
                .filter(r -> r.getStatus() == Rfq.Status.OPEN)
                .collect(Collectors.toList());
                
        List<RfqResponseDto> content = openRfqs.stream()
                .map(this::mapRfqToResponse)
                .collect(Collectors.toList());

        return PageResponseDto.<RfqResponseDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements((long) openRfqs.size())
                .totalPages(1)
                .isLast(true)
                .build();
    }

    @Override
    @Transactional
    public QuotationResponseDto submitQuote(String sellerId, String rfqId, QuotationSubmitRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));

        if (rfq.getStatus() != Rfq.Status.OPEN) {
            throw new IllegalArgumentException("RFQ is not open for quoting");
        }

        Quotation quotation = Quotation.builder()
                .id(UUID.randomUUID().toString())
                .rfqId(rfqId)
                .sellerId(sellerId)
                .sellerName(seller.getName())
                .unitPrice(request.getUnitPrice())
                .freight(request.getFreight())
                .totalPrice(request.getTotalPrice())
                .deliveryDays(request.getDeliveryDays())
                .timeline(request.getTimeline())
                .paymentTerms(request.getPaymentTerms())
                .warranty(request.getWarranty())
                .notes(request.getNotes())
                .validUntil(LocalDateTime.now().plusDays(7)) 
                .status(Quotation.Status.PENDING)
                .build();

        quotation = quotationRepository.save(quotation);
        return mapQuotationToResponse(quotation);
    }
    
    private RfqResponseDto mapRfqToResponse(Rfq rfq) {
        return RfqResponseDto.builder()
                .id(rfq.getId())
                .rfqNumber(rfq.getRfqNumber())
                .buyerId(rfq.getBuyerId())
                .buyerName(rfq.getBuyerName())
                .productId(rfq.getProductId())
                .productName(rfq.getProductName())
                .title(rfq.getTitle())
                .categoryId(rfq.getCategoryId())
                .specifications(rfq.getSpecifications())
                .quantity(rfq.getQuantity())
                .targetPrice(rfq.getTargetPrice())
                .status(rfq.getStatus().name())
                .deliveryLocation(rfq.getDeliveryLocation())
                .deadline(rfq.getDeadline())
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }
    
    private QuotationResponseDto mapQuotationToResponse(Quotation q) {
        return QuotationResponseDto.builder()
                .id(q.getId())
                .rfqId(q.getRfqId())
                .sellerId(q.getSellerId())
                .sellerName(q.getSellerName())
                .unitPrice(q.getUnitPrice())
                .freight(q.getFreight())
                .totalPrice(q.getTotalPrice())
                .deliveryDays(q.getDeliveryDays())
                .timeline(q.getTimeline())
                .paymentTerms(q.getPaymentTerms())
                .warranty(q.getWarranty())
                .notes(q.getNotes())
                .validUntil(q.getValidUntil())
                .status(q.getStatus().name())
                .terms(q.getTerms())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}
