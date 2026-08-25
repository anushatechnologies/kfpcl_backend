package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttachmentDto {

    private String id;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
