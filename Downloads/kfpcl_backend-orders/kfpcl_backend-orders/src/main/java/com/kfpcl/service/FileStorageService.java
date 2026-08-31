package com.kfpcl.service;

import com.kfpcl.dto.FileUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    FileUploadResponseDto storeFile(MultipartFile file);
    List<FileUploadResponseDto> storeMultipleFiles(MultipartFile[] files);
}
