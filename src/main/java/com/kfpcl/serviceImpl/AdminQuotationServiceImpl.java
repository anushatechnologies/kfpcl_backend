package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.entity.Quotation;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.service.AdminQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminQuotationServiceImpl implements AdminQuotationService {

    private final QuotationRepository quotationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<QuotationResponseDto> getQuotations(String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Quotation> quotePage;
        if (StringUtils.hasText(status)) {
            try {
                Quotation.Status qStatus = Quotation.Status.valueOf(status.trim().toUpperCase());
                quotePage = quotationRepository.findByStatus(qStatus, pageable);
            } catch (IllegalArgumentException e) {
                quotePage = quotationRepository.findAll(pageable);
            }
        } else {
            quotePage = quotationRepository.findAll(pageable);
        }

        List<QuotationResponseDto> dtoList = quotePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(quotePage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationResponseDto getQuotationById(String quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "quotationId", quotationId));
        return mapToDto(quotation);
    }

    @Override
    public QuotationResponseDto approveQuotation(String quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "quotationId", quotationId));
        quotation.setStatus(Quotation.Status.ACCEPTED);
        quotation = quotationRepository.save(quotation);
        return mapToDto(quotation);
    }

    @Override
    public QuotationResponseDto rejectQuotation(String quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "quotationId", quotationId));
        quotation.setStatus(Quotation.Status.REJECTED);
        quotation = quotationRepository.save(quotation);
        return mapToDto(quotation);
    }

    private QuotationResponseDto mapToDto(Quotation q) {
        return QuotationResponseDto.builder()
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
                .build();
    }
}
