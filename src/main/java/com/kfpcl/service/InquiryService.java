package com.kfpcl.service;

import com.kfpcl.dto.request.InquiryCreateRequest;
import com.kfpcl.dto.request.InquiryReplyRequest;
import com.kfpcl.dto.response.InquiryResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.entity.enums.InquiryStatus;

public interface InquiryService {

    /**
     * Creates and sends a direct product inquiry from the authenticated buyer.
     */
    InquiryResponse createInquiry(String buyerEmail, InquiryCreateRequest request);

    /**
     * Retrieves paginated list of inquiries submitted by the authenticated buyer.
     */
    PageResponse<InquiryResponse> getBuyerInquiries(String buyerEmail, InquiryStatus status, int page, int size);

    /**
     * Retrieves paginated list of incoming buyer inquiries / leads for the authenticated seller.
     */
    PageResponse<InquiryResponse> getSellerInquiries(String sellerEmail, InquiryStatus status, int page, int size);

    /**
     * Records a seller's reply to a specific buyer inquiry.
     */
    InquiryResponse replyToInquiry(String sellerEmail, Long inquiryId, InquiryReplyRequest request);
}
