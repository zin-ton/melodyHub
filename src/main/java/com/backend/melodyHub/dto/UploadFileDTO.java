package com.backend.melodyHub.dto;

public class UploadFileDTO {
    private final String fileKey;
    private final String preSignedUploadUrl;

    public UploadFileDTO(String fileKey, String preSignedUploadUrl) {
        this.preSignedUploadUrl = preSignedUploadUrl;
        this.fileKey = fileKey;
    }

    public String getPreSignedUploadUrl() {
        return preSignedUploadUrl;
    }

    public String getFileKey() {
        return fileKey;
    }
}
