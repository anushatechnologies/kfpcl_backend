package com.kfpcl.dto;

public class FileUploadResponseDto {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long size;

    public FileUploadResponseDto() {}

    public FileUploadResponseDto(String fileName, String fileUrl, String fileType, long size) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.size = size;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public static FileUploadResponseDtoBuilder builder() { return new FileUploadResponseDtoBuilder(); }

    public static class FileUploadResponseDtoBuilder {
        private String fileName;
        private String fileUrl;
        private String fileType;
        private long size;

        public FileUploadResponseDtoBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public FileUploadResponseDtoBuilder fileUrl(String fileUrl) { this.fileUrl = fileUrl; return this; }
        public FileUploadResponseDtoBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public FileUploadResponseDtoBuilder size(long size) { this.size = size; return this; }

        public FileUploadResponseDto build() {
            return new FileUploadResponseDto(fileName, fileUrl, fileType, size);
        }
    }
}
