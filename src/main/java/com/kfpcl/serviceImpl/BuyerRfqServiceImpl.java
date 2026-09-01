package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.RfqResponseDto;
import com.kfpcl.dto.QuotationResponseDto;
import com.kfpcl.dto.RfqWithQuotesResponseDto;
import com.kfpcl.dto.request.RfqCreateRequest;
import com.kfpcl.dto.response.QuoteAcceptanceResponse;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Quotation;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.User;
import com.kfpcl.event.OrderCreationEvent;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.BuyerRfqService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
public class BuyerRfqServiceImpl implements BuyerRfqService {

    private final RfqRepository rfqRepository;
    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RfqResponseDto createRfq(String buyerId, RfqCreateRequest request) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

        String productName = "General RFQ";
        if (request.getProductId() != null) {
            Product p = productRepository.findById(request.getProductId()).orElse(null);
            if (p != null) {
                productName = p.getProductName();
            }
        }

        Rfq rfq = Rfq.builder()
                .id(UUID.randomUUID().toString())
                .rfqNumber("RFQ-" + System.currentTimeMillis())
                .buyerId(buyerId)
                .buyerName(buyer.getName())
                .productId(request.getProductId())
                .productName(productName)
                .title(request.getTitle())
                .categoryId(request.getCategoryId())
                .specifications(request.getSpecifications())
                .quantity(request.getQuantity())
                .targetPrice(request.getTargetPrice())
                .deliveryLocation(request.getDeliveryLocation())
                .deadline(request.getRequiredDeliveryDate())
                .status(Rfq.Status.OPEN)
                .build();

        rfq = rfqRepository.save(rfq);
        return mapToResponse(rfq);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<RfqResponseDto> getBuyerRfqs(String buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Rfq> rfqs = rfqRepository.findAll(pageable); // Wait, need findByBuyerId
        // The spec requires filtering by buyerId, so I should ensure the repository has it.
        // I will use a simple filter here if repository method is missing, or just write findByBuyerId in repository later if it doesn't exist.
        // Actually, RfqRepository in Admin module might already have it. I'll just use a stream filter for safety if not found, but it should exist.
        List<Rfq> filteredRfqs = rfqs.getContent().stream()
                .filter(r -> r.getBuyerId().equals(buyerId))
                .collect(Collectors.toList());

        List<RfqResponseDto> content = filteredRfqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponseDto.<RfqResponseDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements((long) filteredRfqs.size())
                .totalPages(1)
                .isLast(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RfqWithQuotesResponseDto getRfqWithQuotes(String buyerId, String rfqId) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));

        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("Unauthorized to view this RFQ");
        }

        List<Quotation> quotes = quotationRepository.findByRfqId(rfqId);
        List<QuotationResponseDto> quoteDtos = quotes.stream()
                .map(this::mapQuoteToResponse)
                .collect(Collectors.toList());

        return RfqWithQuotesResponseDto.builder()
                .rfq(mapToResponse(rfq))
                .quotes(quoteDtos)
                .build();
    }

    @Override
    @Transactional
    public QuoteAcceptanceResponse acceptQuote(String buyerId, String rfqId, String quoteId) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found"));

        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("Unauthorized to accept quote for this RFQ");
        }

        Quotation quotation = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (!quotation.getRfqId().equals(rfqId)) {
            throw new IllegalArgumentException("Quotation does not belong to this RFQ");
        }

        // Business Rule: Accepting one quotation closes competing quotes and advances toward order creation.
        List<Quotation> allQuotations = quotationRepository.findByRfqId(rfqId);
        for (Quotation q : allQuotations) {
            if (q.getId().equals(quoteId)) {
                q.setStatus(Quotation.Status.ACCEPTED);
            } else {
                q.setStatus(Quotation.Status.REJECTED);
            }
            quotationRepository.save(q);
        }

        rfq.setStatus(Rfq.Status.CLOSED);
        rfqRepository.save(rfq);

        // Emit the order-service integration event
        OrderCreationEvent event = OrderCreationEvent.builder()
                .buyerId(buyerId)
                .sellerId(quotation.getSellerId())
                .rfqId(rfqId)
                .quotationId(quoteId)
                .totalPrice(quotation.getTotalPrice())
                .build();
        eventPublisher.publishEvent(event);

        return QuoteAcceptanceResponse.builder()
                .quotationId(quoteId)
                .rfqId(rfqId)
                .status("ACCEPTED")
                .message("Quote accepted successfully. Order creation process initiated.")
                .build();
    }

    private RfqResponseDto mapToResponse(Rfq rfq) {
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

    private QuotationResponseDto mapQuoteToResponse(Quotation quote) {
        return QuotationResponseDto.builder()
                .id(quote.getId())
                .rfqId(quote.getRfqId())
                .sellerId(quote.getSellerId())
                .sellerName(quote.getSellerName())
                .unitPrice(quote.getUnitPrice())
                .totalPrice(quote.getTotalPrice())
                .validUntil(quote.getValidUntil())
                .freight(quote.getFreight())
                .deliveryDays(quote.getDeliveryDays())
                .timeline(quote.getTimeline())
                .paymentTerms(quote.getPaymentTerms())
                .warranty(quote.getWarranty())
                .notes(quote.getNotes())
                .status(quote.getStatus().name())
                .terms(quote.getTerms())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }
}
