package com.kfpcl.serviceImpl;

import com.kfpcl.dto.FileUploadResponseDto;
import com.kfpcl.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadPath;

    public FileStorageServiceImpl(@Value("${file.upload.dir:uploads/catalog/}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    @Override
    public FileUploadResponseDto storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.uploadPath.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }

        String fileUrl = "/uploads/" + storedFileName;

        return FileUploadResponseDto.builder()
                .fileName(storedFileName)
                .fileUrl(fileUrl)
                .fileType(file.getContentType())
                .size(file.getSize())
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
