package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.request.InquiryRequest;
import com.kfpcl.dto.response.InquiryResponse;

public interface InquiryService {
    InquiryResponse createInquiry(String buyerId, InquiryRequest request);
    PageResponseDto<InquiryResponse> getBuyerInquiries(String buyerId, int page, int size);
    PageResponseDto<InquiryResponse> getSellerInquiries(String sellerId, int page, int size);
    InquiryResponse replyToInquiry(String sellerId, String inquiryId, String replyMessage);
}
