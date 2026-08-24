package com.kfpcl.service;

import com.kfpcl.dto.ImageUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

    ImageUploadResponseDto uploadCatalogImage(MultipartFile file);
}
