package com.backend.melodyHub.dto;

public class UploadVideoDTO {
    private final String videoKey;
    private final String preSignedUploadUrl;

    public UploadVideoDTO(String videoKey, String preSignedUploadUrl) {
        this.preSignedUploadUrl = preSignedUploadUrl;
        this.videoKey = videoKey;
    }

    public String getPreSignedUploadUrl() {
        return preSignedUploadUrl;
    }

    public String getVideoKey() {
        return videoKey;
    }
}
