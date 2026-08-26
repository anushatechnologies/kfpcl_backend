package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.QuotationSubmitRequest;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.QuotationResponse;
import com.kfpcl.entity.Quotation;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.QuotationStatus;
import com.kfpcl.entity.enums.RFQStatus;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.service.QuotationService;
import com.kfpcl.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final RfqRepository rfqRepository;
    private final SellerProfileService sellerProfileService;

    @Override
    @Transactional
    public QuotationResponse submitQuote(String sellerEmail, Long rfqId, QuotationSubmitRequest request) {
        // Enforce verified seller requirement
        Seller seller = sellerProfileService.getVerifiedSellerEntity(sellerEmail);

        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        if (rfq.getStatus() != RFQStatus.OPEN) {
            throw new IllegalStateException("Quotations cannot be submitted to an RFQ that is not OPEN. Current status: " + rfq.getStatus());
        }

        // Prevent self-quoting
        if (rfq.getBuyer() != null && rfq.getBuyer().getUser() != null &&
                seller.getUser() != null && rfq.getBuyer().getUser().getId().equals(seller.getUser().getId())) {
            throw new IllegalArgumentException("You cannot submit a quote on your own RFQ");
        }

        BigDecimal subTotal = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal freight = request.getFreightCharges() != null ? request.getFreightCharges() : BigDecimal.ZERO;
        BigDecimal tax = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subTotal.add(freight).add(tax);

        // Check if seller has already quoted for this RFQ (Allow updating existing quote if RFQ is open)
        Optional<Quotation> existingQuoteOpt = quotationRepository.findByRfqIdAndSellerId(rfqId, seller.getId());
        Quotation quote;

        if (existingQuoteOpt.isPresent()) {
            quote = existingQuoteOpt.get();
            quote.setUnitPrice(request.getUnitPrice());
            quote.setQuantity(request.getQuantity());
            quote.setFreightCharges(freight);
            quote.setTaxAmount(tax);
            quote.setTotalAmount(totalAmount);
            quote.setDeliveryTimelineDays(request.getDeliveryTimelineDays());
            quote.setPaymentTerms(request.getPaymentTerms().trim());
            quote.setValidUntil(request.getValidUntil());
            quote.setNotes(request.getNotes());
            quote.setStatus(QuotationStatus.SUBMITTED);
        } else {
            quote = Quotation.builder()
                    .rfq(rfq)
                    .seller(seller)
                    .unitPrice(request.getUnitPrice())
                    .quantity(request.getQuantity())
                    .freightCharges(freight)
                    .taxAmount(tax)
                    .totalAmount(totalAmount)
                    .deliveryTimelineDays(request.getDeliveryTimelineDays())
                    .paymentTerms(request.getPaymentTerms().trim())
                    .validUntil(request.getValidUntil())
                    .notes(request.getNotes())
                    .status(QuotationStatus.SUBMITTED)
                    .build();
        }

        Quotation savedQuote = quotationRepository.save(quote);
        return mapToResponse(savedQuote);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuotationResponse> getSellerQuotations(
            String sellerEmail, QuotationStatus status, int page, int size) {

        Seller seller = sellerProfileService.getSellerEntityByEmail(sellerEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Quotation> quotePage = (status != null)
                ? quotationRepository.findBySellerIdAndStatus(seller.getId(), status, pageable)
                : quotationRepository.findBySellerId(seller.getId(), pageable);

        List<QuotationResponse> mapped = quotePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.from(quotePage, mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(Long quoteId) {
        Quotation quote = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quoteId));
        return mapToResponse(quote);
    }

    private QuotationResponse mapToResponse(Quotation quotation) {
        if (quotation == null) {
            return null;
        }

        Rfq rfq = quotation.getRfq();
        Seller seller = quotation.getSeller();

        return QuotationResponse.builder()
                .id(quotation.getId())
                .rfqId(rfq != null ? rfq.getId() : null)
                .rfqTitle(rfq != null ? rfq.getTitle() : null)
                .sellerId(seller != null ? seller.getId() : null)
                .sellerCompanyName(seller != null ? seller.getCompanyName() : null)
                .sellerIsVerified(seller != null ? seller.getIsVerified() : false)
                .sellerRating(seller != null ? seller.getRating() : 0.0)
                .unitPrice(quotation.getUnitPrice())
                .quantity(quotation.getQuantity())
                .unit(rfq != null ? rfq.getUnit() : null)
                .freightCharges(quotation.getFreightCharges())
                .taxAmount(quotation.getTaxAmount())
                .totalAmount(quotation.getTotalAmount())
                .deliveryTimelineDays(quotation.getDeliveryTimelineDays())
                .paymentTerms(quotation.getPaymentTerms())
                .validUntil(quotation.getValidUntil())
                .notes(quotation.getNotes())
                .status(quotation.getStatus())
                .createdAt(quotation.getCreatedAt())
                .updatedAt(quotation.getUpdatedAt())
                .build();
    }
}
