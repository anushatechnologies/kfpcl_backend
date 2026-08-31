package com.kfpcl.service;

import com.kfpcl.dto.ImageUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

    ImageUploadResponseDto uploadCatalogImage(MultipartFile file);

    ImageUploadResponseDto uploadConversationAttachment(String conversationId, MultipartFile file);

    String uploadBase64Image(String imageDataUri);

    void deleteFile(String fileUrl);

    // Entity‑specific image upload methods
    ImageUploadResponseDto uploadCategoryImage(MultipartFile file);
    ImageUploadResponseDto uploadSubcategoryImage(MultipartFile file);
    ImageUploadResponseDto uploadProductImage(MultipartFile file);

    // Presigned URL generation
    String generatePresignedUrl(String storedUrl);
}
