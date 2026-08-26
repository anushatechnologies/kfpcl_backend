package com.kfpcl.serviceImpl;

import com.kfpcl.dto.response.QuotationResponse;
import com.kfpcl.dto.response.RfqComparisonResponse;
import com.kfpcl.dto.response.RfqResponse;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Quotation;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.Seller;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.service.BuyerRfqComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerRfqComparisonServiceImpl implements BuyerRfqComparisonService {

    private final RfqRepository rfqRepository;
    private final BuyerRepository buyerRepository;
    private final QuotationRepository quotationRepository;

    @Override
    @Transactional(readOnly = true)
    public RfqComparisonResponse getRfqQuotationsComparison(
            String buyerEmail, Long rfqId, String sortBy, String sortDir) {

        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user: " + buyerEmail));

        // Enforce strict ownership: RFQ must belong to the authenticated buyer
        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        List<Quotation> quotations = quotationRepository.findByRfqId(rfqId);

        // Sorting
        boolean isAsc = sortDir == null || sortDir.equalsIgnoreCase("ASC");
        Comparator<Quotation> comparator = getComparator(sortBy, isAsc);
        quotations.sort(comparator);

        // Metrics Computation
        BigDecimal lowest = null;
        BigDecimal highest = null;
        BigDecimal average = null;

        if (!quotations.isEmpty()) {
            lowest = quotations.stream()
                    .map(Quotation::getTotalAmount)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            highest = quotations.stream()
                    .map(Quotation::getTotalAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal sum = quotations.stream()
                    .map(Quotation::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            average = sum.divide(BigDecimal.valueOf(quotations.size()), 2, RoundingMode.HALF_UP);
        }

        List<QuotationResponse> quoteResponses = quotations.stream()
                .map(this::mapToQuotationResponse)
                .collect(Collectors.toList());

        RfqResponse rfqResponse = mapToRfqResponse(rfq, (long) quotations.size());

        return RfqComparisonResponse.builder()
                .rfqDetails(rfqResponse)
                .totalQuotesCount(quotations.size())
                .lowestQuoteAmount(lowest)
                .highestQuoteAmount(highest)
                .averageQuoteAmount(average)
                .quotations(quoteResponses)
                .build();
    }

    private Comparator<Quotation> getComparator(String sortBy, boolean isAsc) {
        Comparator<Quotation> comp;
        String field = sortBy != null ? sortBy.toLowerCase() : "totalamount";

        switch (field) {
            case "unitprice" -> comp = Comparator.comparing(Quotation::getUnitPrice);
            case "deliverytimeline", "timeline", "deliverytimelinedays" -> comp = Comparator.comparing(Quotation::getDeliveryTimelineDays);
            case "rating", "sellerrating" -> comp = Comparator.comparing(q -> q.getSeller() != null ? q.getSeller().getRating() : 0.0);
            case "createdat" -> comp = Comparator.comparing(Quotation::getCreatedAt);
            default -> comp = Comparator.comparing(Quotation::getTotalAmount);
        }

        return isAsc ? comp : comp.reversed();
    }

    private QuotationResponse mapToQuotationResponse(Quotation quotation) {
        if (quotation == null) {
            return null;
        }

        Seller seller = quotation.getSeller();
        Rfq rfq = quotation.getRfq();

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

    private RfqResponse mapToRfqResponse(Rfq rfq, Long totalQuotes) {
        if (rfq == null) {
            return null;
        }

        Buyer buyer = rfq.getBuyer();
        Category category = rfq.getCategory();

        return RfqResponse.builder()
                .id(rfq.getId())
                .buyerId(buyer != null ? buyer.getId() : null)
                .buyerCompanyName(buyer != null ? buyer.getCompanyName() : null)
                .buyerContactPerson(buyer != null ? buyer.getContactPerson() : null)
                .buyerEmail(buyer != null && buyer.getUser() != null ? buyer.getUser().getEmail() : null)
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
                .totalQuotesCount(totalQuotes)
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }
}
