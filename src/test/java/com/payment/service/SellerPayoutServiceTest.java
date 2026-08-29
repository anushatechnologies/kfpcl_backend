package com.payment.service;

import com.payment.dto.payout.SellerPayoutResponse;
import com.payment.entity.SellerPayout;
import com.payment.entity.enums.PayoutStatus;
import com.payment.entity.enums.UserRole;
import com.payment.repository.SellerPayoutRepository;
import com.payment.security.SecurityUtils;
import com.payment.security.UserContext;
import com.payment.service.impl.SellerPayoutServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerPayoutServiceTest {

    @Mock
    private SellerPayoutRepository sellerPayoutRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private SellerPayoutServiceImpl sellerPayoutService;

    private UserContext sellerUser;

    @BeforeEach
    void setUp() {
        sellerUser = UserContext.builder()
                .userId("SELLER-501")
                .email("seller501@kfpcl.com")
                .role(UserRole.SELLER)
                .build();
    }

    @Test
    void testGetSellerPayouts_Success() {
        when(securityUtils.getCurrentUser()).thenReturn(sellerUser);

        SellerPayout payout = SellerPayout.builder()
                .id(1L)
                .sellerId("SELLER-501")
                .orderId("ORD-1001")
                .grossAmount(new BigDecimal("50000.00"))
                .platformFee(new BigDecimal("1000.00"))
                .taxDeduction(new BigDecimal("500.00"))
                .netAmount(new BigDecimal("48500.00"))
                .status(PayoutStatus.COMPLETED)
                .bankReference("PAYOUT-NEFT-001")
                .initiatedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(sellerPayoutRepository.findBySellerId("SELLER-501")).thenReturn(List.of(payout));
        when(sellerPayoutRepository.findSellerPayouts(
                eq("SELLER-501"), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(payout)));

        SellerPayoutResponse response = sellerPayoutService.getSellerPayouts(
                null, null, null, 0, 10, "initiatedAt", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("ORD-1001", response.getContent().get(0).getOrderId());
        assertEquals(new BigDecimal("50000.00"), response.getContent().get(0).getGrossAmount());
        assertEquals(new BigDecimal("1000.00"), response.getContent().get(0).getPlatformFee());
        assertEquals(new BigDecimal("500.00"), response.getContent().get(0).getTaxDeduction());
        assertEquals(new BigDecimal("48500.00"), response.getContent().get(0).getNetAmount());
        assertEquals(PayoutStatus.COMPLETED, response.getContent().get(0).getStatus());

        assertNotNull(response.getSummary());
        assertEquals(new BigDecimal("50000.00"), response.getSummary().getTotalGross());
        assertEquals(new BigDecimal("1000.00"), response.getSummary().getTotalPlatformFees());
        assertEquals(new BigDecimal("500.00"), response.getSummary().getTotalTaxDeductions());
        assertEquals(new BigDecimal("48500.00"), response.getSummary().getTotalNetPayouts());
        assertEquals(1, response.getSummary().getCompletedCount());

        verify(securityUtils).getCurrentUser();
    }
}
