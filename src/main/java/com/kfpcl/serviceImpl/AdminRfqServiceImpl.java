package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.entity.Rfq;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.service.AdminRfqService;
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
public class AdminRfqServiceImpl implements AdminRfqService {

    private final RfqRepository rfqRepository;
    private final QuotationRepository quotationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<RfqResponseDto> getRfqs(String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Rfq> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                try {
                    Rfq.Status rStatus = Rfq.Status.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), rStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("rfqNumber")), pattern);
                Predicate prodMatch = cb.like(cb.lower(root.get("productName")), pattern);
                Predicate buyerMatch = cb.like(cb.lower(root.get("buyerName")), pattern);
                predicates.add(cb.or(numMatch, prodMatch, buyerMatch));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Rfq> rfqPage = rfqRepository.findAll(spec, pageable);
        List<RfqResponseDto> dtoList = rfqPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(rfqPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public RfqResponseDto getRfqById(String rfqId) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("Rfq", "rfqId", rfqId));
        return mapToDto(rfq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponseDto> getQuotationsForRfq(String rfqId) {
        if (!rfqRepository.existsById(rfqId)) {
            throw new ResourceNotFoundException("Rfq", "rfqId", rfqId);
        }

        return quotationRepository.findByRfqId(rfqId).stream()
                .map(q -> QuotationResponseDto.builder()
                        .id(q.getId())
                        .rfqId(q.getRfqId())
                        .sellerId(q.getSellerId())
                        .sellerName(q.getSellerName())
                        .unitPrice(q.getUnitPrice())
                        .totalPrice(q.getTotalPrice())
                        .validUntil(q.getValidUntil())
                        .status(q.getStatus().name())
                        .terms(q.getTerms())
                        .createdAt(q.getCreatedAt())
                        .updatedAt(q.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private RfqResponseDto mapToDto(Rfq rfq) {
        int totalQuotes = quotationRepository.findByRfqId(rfq.getId()).size();

        return RfqResponseDto.builder()
                .id(rfq.getId())
                .rfqNumber(rfq.getRfqNumber())
                .buyerId(rfq.getBuyerId())
                .buyerName(rfq.getBuyerName())
                .productId(rfq.getProductId())
                .productName(rfq.getProductName())
                .quantity(rfq.getQuantity())
                .targetPrice(rfq.getTargetPrice())
                .status(rfq.getStatus().name())
                .deliveryLocation(rfq.getDeliveryLocation())
                .notes(rfq.getNotes())
                .deadline(rfq.getDeadline())
                .totalQuotations(totalQuotes)
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }
}
