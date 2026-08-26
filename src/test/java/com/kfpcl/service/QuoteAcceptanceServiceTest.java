package com.kfpcl.service;

import com.kfpcl.dto.response.QuoteAcceptanceResponse;
import com.kfpcl.entity.*;
import com.kfpcl.entity.enums.QuotationStatus;
import com.kfpcl.entity.enums.RFQStatus;
import com.kfpcl.event.OrderCreationEvent;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.QuotationRepository;
import com.kfpcl.repository.RfqRepository;
import com.kfpcl.serviceImpl.QuoteAcceptanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteAcceptanceServiceTest {

    @Mock
    private RfqRepository rfqRepository;

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuoteAcceptanceServiceImpl quoteAcceptanceService;

    private Buyer buyer;
    private Seller winningSeller;
    private Rfq rfq;
    private Quotation winningQuote;
    private Quotation competingQuote;

    @BeforeEach
    void setUp() {
        User buyerUser = User.builder().id(1L).email("buyer@kfpcl.com").build();
        buyer = Buyer.builder().id(1L).user(buyerUser).companyName("Metro Foods").build();

        User sellerUser = User.builder().id(2L).email("seller@kfpcl.com").build();
        winningSeller = Seller.builder().id(1L).user(sellerUser).companyName("Agri Co").build();

        rfq = Rfq.builder()
                .id(100L)
                .buyer(buyer)
                .title("100 MT Milling Wheat")
                .quantity(100)
                .unit("MT")
                .status(RFQStatus.OPEN)
                .build();

        winningQuote = Quotation.builder()
                .id(10L)
                .rfq(rfq)
                .seller(winningSeller)
                .unitPrice(new BigDecimal("31000.00"))
                .quantity(100)
                .freightCharges(new BigDecimal("20000.00"))
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("3120000.00"))
                .deliveryTimelineDays(7)
                .paymentTerms("20% Advance, 80% on Delivery")
                .validUntil(LocalDate.now().plusDays(30))
                .status(QuotationStatus.SUBMITTED)
                .build();

        competingQuote = Quotation.builder()
                .id(11L)
                .rfq(rfq)
                .seller(Seller.builder().id(2L).companyName("Other Seller").build())
                .unitPrice(new BigDecimal("32000.00"))
                .quantity(100)
                .totalAmount(new BigDecimal("3220000.00"))
                .status(QuotationStatus.SUBMITTED)
                .build();
    }

    @Test
    @DisplayName("Should accept quote, close competing quotes, award RFQ, and publish OrderCreationEvent")
    void acceptQuote_Success() {
        when(buyerRepository.findByUserEmail("buyer@kfpcl.com")).thenReturn(Optional.of(buyer));
        when(rfqRepository.findByIdAndBuyerId(100L, 1L)).thenReturn(Optional.of(rfq));
        when(quotationRepository.findById(10L)).thenReturn(Optional.of(winningQuote));
        when(quotationRepository.findByRfqIdAndIdNot(100L, 10L)).thenReturn(List.of(competingQuote));

        QuoteAcceptanceResponse response = quoteAcceptanceService.acceptQuote("buyer@kfpcl.com", 100L, 10L);

        assertThat(response).isNotNull();
        assertThat(response.getAcceptedQuoteId()).isEqualTo(10L);
        assertThat(response.getRfqStatus()).isEqualTo(RFQStatus.AWARDED);
        assertThat(response.getClosedCompetingQuotesCount()).isEqualTo(1);

        assertThat(winningQuote.getStatus()).isEqualTo(QuotationStatus.ACCEPTED);
        assertThat(competingQuote.getStatus()).isEqualTo(QuotationStatus.CLOSED);
        assertThat(rfq.getStatus()).isEqualTo(RFQStatus.AWARDED);

        verify(eventPublisher).publishEvent(any(OrderCreationEvent.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when RFQ is not in OPEN status")
    void acceptQuote_RfqNotOpen() {
        rfq.setStatus(RFQStatus.AWARDED);
        when(buyerRepository.findByUserEmail("buyer@kfpcl.com")).thenReturn(Optional.of(buyer));
        when(rfqRepository.findByIdAndBuyerId(100L, 1L)).thenReturn(Optional.of(rfq));

        assertThatThrownBy(() -> quoteAcceptanceService.acceptQuote("buyer@kfpcl.com", 100L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot accept quote for an RFQ that is not OPEN");

        verify(eventPublisher, never()).publishEvent(any());
    }
}
