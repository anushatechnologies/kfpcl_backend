package com.kfpcl.controller;

import com.kfpcl.dto.FileUploadResponseDto;
import com.kfpcl.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<FileUploadResponseDto> uploadSingleFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponseDto response = fileStorageService.storeFile(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<FileUploadResponseDto>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<FileUploadResponseDto> response = fileStorageService.storeMultipleFiles(files);
        return ResponseEntity.ok(response);
    }
}
