package com.backend.melodyHub.component;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class S3Service {

    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Autowired
    public S3Service(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }


    public String generateUploadUrl(String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(15)).putObjectRequest(objectRequest).build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public String generatePresignedPreviewUrl(String key) {
        if (key != null && !key.trim().isEmpty()) {
            key = "images/" + key.replaceAll("\\.[^.]*$", ".jpg");
        }
        return generatePresignedUrl(key);
    }

    public String generatePresignedVideoUrl(String key) {
        if (key != null && !key.trim().isEmpty()) {
            key = "videos/" + key.replaceAll("\\.[^.]*$", ".mp4");
        }
        return generatePresignedUrl(key);
    }

    private String generatePresignedUrl(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofHours(10)).getObjectRequest(getObjectRequest).build();
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
    }
}

