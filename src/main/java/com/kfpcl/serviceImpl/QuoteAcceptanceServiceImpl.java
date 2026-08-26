package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.QuoteAcceptanceResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Quotation;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.QuotationStatus;
import com.kfpcl.entity.enums.RFQStatus;
import com.kfpcl.event.OrderCreationEvent;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.service.QuoteAcceptanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteAcceptanceServiceImpl implements QuoteAcceptanceService {

    private final RfqRepository rfqRepository;
    private final BuyerRepository buyerRepository;
    private final QuotationRepository quotationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuoteAcceptanceResponse acceptQuote(String buyerEmail, Long rfqId, Long quoteId) {
        log.info("Processing quote acceptance: buyer={}, rfqId={}, quoteId={}", buyerEmail, rfqId, quoteId);

        // 1. Verify Buyer Identity
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        // 2. Verify RFQ Ownership and Existence
        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        // 3. Verify RFQ is in OPEN status
        if (rfq.getStatus() != RFQStatus.OPEN) {
            throw new IllegalStateException(
                    "Cannot accept quote for an RFQ that is not OPEN. Current RFQ status: " + rfq.getStatus()
            );
        }

        // 4. Verify Quotation belongs to this RFQ
        Quotation selectedQuote = quotationRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quoteId));

        if (!selectedQuote.getRfq().getId().equals(rfqId)) {
            throw new IllegalArgumentException(
                    "Quotation #" + quoteId + " does not belong to RFQ #" + rfqId
            );
        }

        // 5. Verify Quotation is eligible for acceptance
        if (selectedQuote.getStatus() != QuotationStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Quotation is not in SUBMITTED status. Current quotation status: " + selectedQuote.getStatus()
            );
        }

        if (selectedQuote.getValidUntil() != null && selectedQuote.getValidUntil().isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    "Selected quotation expired on " + selectedQuote.getValidUntil() + ". Cannot accept expired quotation."
            );
        }

        // 6. Mark selected quotation as ACCEPTED
        selectedQuote.setStatus(QuotationStatus.ACCEPTED);
        quotationRepository.save(selectedQuote);

        // 7. Mark competing quotations as CLOSED
        List<Quotation> competingQuotes = quotationRepository.findByRfqIdAndIdNot(rfqId, quoteId);
        int closedCount = 0;
        for (Quotation compQuote : competingQuotes) {
            if (compQuote.getStatus() == QuotationStatus.SUBMITTED) {
                compQuote.setStatus(QuotationStatus.CLOSED);
                quotationRepository.save(compQuote);
                closedCount++;
            }
        }

        // 8. Mark RFQ as AWARDED
        rfq.setStatus(RFQStatus.AWARDED);
        rfqRepository.save(rfq);

        // 9. Generate tracking reference
        String trackingRef = "ORD-RFQ-" + rfqId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Seller seller = selectedQuote.getSeller();

        // 10. Construct & Emit OrderCreationEvent
        OrderCreationEvent event = OrderCreationEvent.builder()
                .eventId(trackingRef)
                .rfqId(rfq.getId())
                .rfqTitle(rfq.getTitle())
                .quotationId(selectedQuote.getId())
                .buyerId(buyer.getId())
                .buyerEmail(buyer.getUser() != null ? buyer.getUser().getEmail() : null)
                .buyerCompanyName(buyer.getCompanyName())
                .sellerId(seller.getId())
                .sellerEmail(seller.getUser() != null ? seller.getUser().getEmail() : null)
                .sellerCompanyName(seller.getCompanyName())
                .quantity(selectedQuote.getQuantity())
                .unit(rfq.getUnit())
                .unitPrice(selectedQuote.getUnitPrice())
                .freightCharges(selectedQuote.getFreightCharges())
                .taxAmount(selectedQuote.getTaxAmount())
                .totalAmount(selectedQuote.getTotalAmount())
                .deliveryLocation(rfq.getDeliveryLocation())
                .paymentTerms(selectedQuote.getPaymentTerms())
                .deliveryTimelineDays(selectedQuote.getDeliveryTimelineDays())
                .eventTimestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);

        log.info("Quote acceptance completed successfully. Event emitted with ref: {}", trackingRef);

        return QuoteAcceptanceResponse.builder()
                .rfqId(rfq.getId())
                .rfqTitle(rfq.getTitle())
                .acceptedQuoteId(selectedQuote.getId())
                .winningSellerId(seller.getId())
                .winningSellerCompanyName(seller.getCompanyName())
                .unitPrice(selectedQuote.getUnitPrice())
                .quantity(selectedQuote.getQuantity())
                .unit(rfq.getUnit())
                .freightCharges(selectedQuote.getFreightCharges())
                .taxAmount(selectedQuote.getTaxAmount())
                .totalOrderAmount(selectedQuote.getTotalAmount())
                .deliveryTimelineDays(selectedQuote.getDeliveryTimelineDays())
                .paymentTerms(selectedQuote.getPaymentTerms())
                .rfqStatus(rfq.getStatus())
                .closedCompetingQuotesCount(closedCount)
                .orderTrackingReference(trackingRef)
                .acceptedAt(LocalDateTime.now())
                .message("Quotation accepted successfully. Competing quotes closed and order processing event emitted.")
                .build();
    }
}
