package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.UploadFileDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "AWS Bucket Controller")
public class AwsBucketController {
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;

    @Autowired
    public AwsBucketController(JwtUtil jwtUtil, S3Service s3Service) {
        this.jwtUtil = jwtUtil;
        this.s3Service = s3Service;
    }

    @GetMapping("/getUploadVideoLink")
    public ResponseEntity<?> getUploadVideoLink(@RequestHeader String token, @RequestHeader String filename) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String dbKey = UUID.randomUUID() + filename;
        String key = "videos/" + dbKey;
        String uploadUrl = s3Service.generateUploadUrl(key);
        return ResponseEntity.ok(new UploadFileDTO(dbKey, uploadUrl));
    }

    @GetMapping("/getUploadImageLink")
    public ResponseEntity<?> getUploadImageLink(@RequestHeader String token, @RequestHeader String filename) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String key = "images/" + UUID.randomUUID() + filename;
        String uploadUrl = s3Service.generateUploadUrl(key);
        return ResponseEntity.ok(new UploadFileDTO(key, uploadUrl));
    }

    @GetMapping("/getUploadLeadsheetLink")
    public ResponseEntity<?> getUploadLeadsheetLink(@RequestHeader String token, @RequestHeader String filename) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String key = "leadsheets/" + UUID.randomUUID() + filename;
        String uploadUrl = s3Service.generateUploadUrl(key);
        return ResponseEntity.ok(new UploadFileDTO(key, uploadUrl));
    }
}
