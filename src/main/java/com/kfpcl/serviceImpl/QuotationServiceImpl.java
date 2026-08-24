package com.kfpcl.serviceImpl;

import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.QuotationCompareResponse;
import com.kfpcl.dto.QuotationResponse;
import com.kfpcl.dto.SupplierSummaryDto;
import com.kfpcl.entity.*;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.exception.UnprocessableEntityException;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.util.SecurityUtils;
import com.kfpcl.service.OrderService;
import com.kfpcl.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final RfqRepository rfqRepository;
    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<QuotationResponse> getQuotationsForRfq(String rfqId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        return quotationRepository.findByRfqIdOrderByQuotedPriceAsc(rfqId)
                .stream()
                .map(this::mapToQuotationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationCompareResponse compareQuotations(String rfqId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        List<Quotation> quotations = quotationRepository.findByRfqIdOrderByQuotedPriceAsc(rfqId);
        List<QuotationResponse> quotationDtos = quotations.stream()
                .map(this::mapToQuotationResponse)
                .collect(Collectors.toList());

        BigDecimal lowestPrice = quotations.stream()
                .map(Quotation::getQuotedPrice)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Integer fastestLeadTime = quotations.stream()
                .filter(q -> q.getLeadTimeDays() != null)
                .map(Quotation::getLeadTimeDays)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return QuotationCompareResponse.builder()
                .rfqId(rfq.getId())
                .productTitle(rfq.getProductTitle())
                .rfqQuantity(rfq.getQuantity())
                .targetPrice(rfq.getTargetPrice())
                .lowestQuotedPrice(lowestPrice)
                .fastestLeadTimeDays(fastestLeadTime)
                .quotations(quotationDtos)
                .build();
    }

    @Override
    @Transactional
    public QuotationResponse rejectQuotation(String rfqId, String quotationId) {
        Buyer buyer = securityUtils.getCurrentBuyer();
        rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        Quotation quotation = quotationRepository.findByIdAndRfqId(quotationId, rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quotationId));

        if (quotation.getStatus() != Quotation.Status.PENDING) {
            throw new UnprocessableEntityException("Cannot reject quotation in status: " + quotation.getStatus());
        }

        quotation.setStatus(Quotation.Status.REJECTED);
        Quotation updated = quotationRepository.save(quotation);
        return mapToQuotationResponse(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuyerOrderResponse acceptQuotation(String rfqId, String quotationId) {
        Buyer buyer = securityUtils.getCurrentBuyer();

        // 1. Verify buyer owns RFQ
        Rfq rfq = rfqRepository.findByIdAndBuyerId(rfqId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", rfqId));

        // 2. Verify RFQ allows acceptance
        if (rfq.getStatus() != Rfq.Status.OPEN && rfq.getStatus() != Rfq.Status.QUOTATIONS_RECEIVED) {
            throw new UnprocessableEntityException("Cannot accept quotation for RFQ in status: " + rfq.getStatus());
        }

        // 3. Verify quotation belongs to RFQ
        Quotation quotation = quotationRepository.findByIdAndRfqId(quotationId, rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", quotationId));

        // 4. Verify quotation is in PENDING state
        if (quotation.getStatus() != Quotation.Status.PENDING) {
            throw new UnprocessableEntityException("Cannot accept quotation with status: " + quotation.getStatus());
        }

        // 5. Set selected quotation -> ACCEPTED
        quotation.setStatus(Quotation.Status.ACCEPTED);
        quotationRepository.save(quotation);

        // 6. Close/reject all competing quotations
        List<Quotation> otherQuotations = quotationRepository.findByRfqId(rfqId);
        for (Quotation other : otherQuotations) {
            if (!other.getId().equals(quotationId) && other.getStatus() == Quotation.Status.PENDING) {
                other.setStatus(Quotation.Status.REJECTED);
                quotationRepository.save(other);
            }
        }

        // 7. Update RFQ status -> ACCEPTED
        rfq.setStatus(Rfq.Status.ACCEPTED);
        rfqRepository.save(rfq);

        // 8. Integrate with Order Creation
        Order order = orderService.createOrderFromQuotation(quotation, buyer.getAddress());

        // 9. Return buyer order response
        return orderService.getBuyerOrderById(order.getId());
    }

    private QuotationResponse mapToQuotationResponse(Quotation quotation) {
        SupplierSummaryDto supplierDto = null;
        if (quotation.getSupplier() != null) {
            supplierDto = SupplierSummaryDto.builder()
                    .id(quotation.getSupplier().getId())
                    .companyName(quotation.getSupplier().getCompanyName())
                    .gstVerified(quotation.getSupplier().getGstVerified())
                    .isVerified(quotation.getSupplier().getIsVerified())
                    .build();
        }

        return QuotationResponse.builder()
                .id(quotation.getId())
                .rfqId(quotation.getRfq() != null ? quotation.getRfq().getId() : null)
                .supplier(supplierDto)
                .quotedPrice(quotation.getQuotedPrice())
                .quantity(quotation.getQuantity())
                .leadTimeDays(quotation.getLeadTimeDays())
                .validUntil(quotation.getValidUntil())
                .notes(quotation.getNotes())
                .status(quotation.getStatus().name())
                .createdAt(quotation.getCreatedAt())
                .build();
    }
}
