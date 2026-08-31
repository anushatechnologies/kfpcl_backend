package com.kfpcl.util;

import com.kfpcl.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Backward-compatible adapter for Base64 image data submitted in catalog DTOs. */
@Component
@RequiredArgsConstructor
public class ImageUtils {

    private final ImageUploadService imageUploadService;

    public String processBase64Image(String imageUrl) {
        return imageUploadService.uploadBase64Image(imageUrl);
    }

    public String generatePresignedUrl(String storedUrl) {
        return imageUploadService.generatePresignedUrl(storedUrl);
    }
}
