package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponseDto {

    private String fileName;
    private String fileUrl;
    private long fileSize;
    private String contentType;

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
