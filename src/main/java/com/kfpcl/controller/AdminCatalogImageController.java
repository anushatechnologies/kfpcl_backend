package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.ImageUploadResponseDto;
import com.kfpcl.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
public class AdminCatalogImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponseDto>> uploadCatalogImage(
            @RequestParam("file") MultipartFile file) {

        ImageUploadResponseDto uploadResult = imageUploadService.uploadCatalogImage(file);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(uploadResult, "Catalog image uploaded successfully"));
    }
}
