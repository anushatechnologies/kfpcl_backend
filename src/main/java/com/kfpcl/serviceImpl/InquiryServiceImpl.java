package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.InquiryCreateRequest;
import com.kfpcl.dto.request.InquiryReplyRequest;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Inquiry;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.InquiryStatus;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.InquiryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.SellerRepository;
import com.kfpcl.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public InquiryResponse createInquiry(String buyerEmail, InquiryCreateRequest request) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isPubliclyVisible()) {
            throw new IllegalArgumentException("Cannot send inquiry for an inactive or unapproved product");
        }

        Seller seller = product.getSeller();
        if (seller == null) {
            throw new IllegalStateException("Product is not linked to any valid seller");
        }

        // Prevent self-inquiry if same user
        if (seller.getUser() != null && buyer.getUser() != null &&
                seller.getUser().getId().equals(buyer.getUser().getId())) {
            throw new IllegalArgumentException("You cannot send an inquiry on your own product listing");
        }

        Inquiry inquiry = Inquiry.builder()
                .buyer(buyer)
                .seller(seller)
                .product(product)
                .subject(request.getSubject().trim())
                .message(request.getMessage().trim())
                .quantity(request.getQuantity())
                .targetPrice(request.getTargetPrice())
                .status(InquiryStatus.PENDING)
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return mapToResponse(savedInquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> getBuyerInquiries(
            String buyerEmail, InquiryStatus status, int page, int size) {

        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Inquiry> inquiryPage = (status != null)
                ? inquiryRepository.findByBuyerIdAndStatus(buyer.getId(), status, pageable)
                : inquiryRepository.findByBuyerId(buyer.getId(), pageable);

        List<InquiryResponse> mapped = inquiryPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(inquiryPage, mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InquiryResponse> getSellerInquiries(
            String sellerEmail, InquiryStatus status, int page, int size) {

        Seller seller = sellerRepository.findByUserEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user: " + sellerEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Inquiry> inquiryPage = (status != null)
                ? inquiryRepository.findBySellerIdAndStatus(seller.getId(), status, pageable)
                : inquiryRepository.findBySellerId(seller.getId(), pageable);

        List<InquiryResponse> mapped = inquiryPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(inquiryPage, mapped);
    }

    @Override
    @Transactional
    public InquiryResponse replyToInquiry(String sellerEmail, Long inquiryId, InquiryReplyRequest request) {
        Seller seller = sellerRepository.findByUserEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user: " + sellerEmail));

        Inquiry inquiry = inquiryRepository.findByIdAndSellerId(inquiryId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", inquiryId));

        inquiry.setSellerReply(request.getReplyMessage().trim());
        inquiry.setStatus(InquiryStatus.REPLIED);
        inquiry.setRepliedAt(LocalDateTime.now());

        Inquiry updatedInquiry = inquiryRepository.save(inquiry);
        return mapToResponse(updatedInquiry);
    }

    private InquiryResponse mapToResponse(Inquiry inquiry) {
        if (inquiry == null) {
            return null;
        }

        Product product = inquiry.getProduct();
        Buyer buyer = inquiry.getBuyer();
        Seller seller = inquiry.getSeller();

        return InquiryResponse.builder()
                .id(inquiry.getId())
                .buyerId(buyer != null ? buyer.getId() : null)
                .buyerCompanyName(buyer != null ? buyer.getCompanyName() : null)
                .buyerContactPerson(buyer != null ? buyer.getContactPerson() : null)
                .buyerEmail(buyer != null && buyer.getUser() != null ? buyer.getUser().getEmail() : null)
                .sellerId(seller != null ? seller.getId() : null)
                .sellerCompanyName(seller != null ? seller.getCompanyName() : null)
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .productSlug(product != null ? product.getSlug() : null)
                .productBasePrice(product != null ? product.getBasePrice() : null)
                .productUnit(product != null ? product.getUnit() : null)
                .productPrimaryImageUrl(product != null ? product.getPrimaryImageUrl() : null)
                .subject(inquiry.getSubject())
                .message(inquiry.getMessage())
                .quantity(inquiry.getQuantity())
                .targetPrice(inquiry.getTargetPrice())
                .status(inquiry.getStatus())
                .sellerReply(inquiry.getSellerReply())
                .repliedAt(inquiry.getRepliedAt())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }
}
