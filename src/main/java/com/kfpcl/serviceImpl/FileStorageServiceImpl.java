
package com.kfpcl.serviceImpl;

import com.kfpcl.dto.FileUploadResponseDto;
import com.kfpcl.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kfpcl.service.ImageUploadService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final ImageUploadService imageUploadService;

    public FileStorageServiceImpl(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @Override
    public FileUploadResponseDto storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        // Upload to S3 using the existing ImageUploadService
        var s3Response = imageUploadService.uploadCatalogImage(file);

        // Generate the full presigned URL so the frontend can immediately display it
        String fullUrl = imageUploadService.generatePresignedUrl(s3Response.getImageKey());

        return FileUploadResponseDto.builder()
                .fileName(s3Response.getFileName())
                .fileUrl(fullUrl)
                .fileType(s3Response.getContentType())
                .size((long) s3Response.getFileSize())
                .build();
    }

    @Override
    public List<FileUploadResponseDto> storeMultipleFiles(MultipartFile[] files) {
        List<FileUploadResponseDto> responseList = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    responseList.add(storeFile(file));
                }
            }
        }
        return responseList;
    }
}
