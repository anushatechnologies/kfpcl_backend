package com.payment.service;

import com.payment.dto.lc.LcStatusResponse;
import com.payment.dto.lc.LcUploadRequest;
import com.payment.dto.lc.LcUploadResponse;
import com.payment.dto.lc.LcVerificationRequest;
import org.springframework.web.multipart.MultipartFile;

public interface LetterOfCreditService {

    LcUploadResponse uploadLetterOfCredit(LcUploadRequest request, MultipartFile file, String clientIp);

    LcStatusResponse getLetterOfCreditStatus(String orderId);

    LcStatusResponse verifyLetterOfCredit(String orderId, LcVerificationRequest request, String clientIp);
}
