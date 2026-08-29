package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.request.InquiryRequest;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.entity.Inquiry;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.User;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.InquiryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InquiryResponse createInquiry(String buyerId, InquiryRequest request) {
        if (!userRepository.existsById(request.getSellerId())) {
            throw new ResourceNotFoundException("Seller not found");
        }

        Inquiry inquiry = Inquiry.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(buyerId)
                .sellerId(request.getSellerId())
                .productId(request.getProductId())
                .message(request.getMessage())
                .status(Inquiry.Status.PENDING)
                .build();
        
        inquiry = inquiryRepository.save(inquiry);
        return mapToResponse(inquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InquiryResponse> getBuyerInquiries(String buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inquiry> inquiries = inquiryRepository.findByBuyerId(buyerId, pageable);
        return createPageResponse(inquiries);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InquiryResponse> getSellerInquiries(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inquiry> inquiries = inquiryRepository.findBySellerId(sellerId, pageable);
        return createPageResponse(inquiries);
    }

    @Override
    @Transactional
    public InquiryResponse replyToInquiry(String sellerId, String inquiryId, String replyMessage) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
                
        if (!inquiry.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Unauthorized to reply to this inquiry");
        }
        
        inquiry.setStatus(Inquiry.Status.REPLIED);
        inquiry = inquiryRepository.save(inquiry);
        return mapToResponse(inquiry);
    }
    
    private InquiryResponse mapToResponse(Inquiry inquiry) {
        Product product = inquiry.getProductId() != null ? 
                productRepository.findById(inquiry.getProductId()).orElse(null) : null;
        User seller = userRepository.findById(inquiry.getSellerId()).orElse(null);
        
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .buyerId(inquiry.getBuyerId())
                .sellerId(inquiry.getSellerId())
                .sellerName(seller != null ? seller.getName() : "Unknown")
                .productId(inquiry.getProductId())
                .productName(product != null ? product.getProductName() : null)
                .message(inquiry.getMessage())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
    
    private PageResponseDto<InquiryResponse> createPageResponse(Page<Inquiry> page) {
        List<InquiryResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponseDto.<InquiryResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}
