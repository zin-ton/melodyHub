package com.backend.melodyHub.dto;

import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public class EditPostDTO {
    private Integer id;
    @Nullable
    private String s3Key;
    @Nullable
    private String description;
    @Nullable
    private String name;
    @Nullable
    private String leadsheetKey;
    @Nullable
    private List<Integer> categories;

    public EditPostDTO(Integer id, @Nullable String s3Key, @Nullable String description, @Nullable String name, @Nullable String leadsheetKey, @Nullable List<Integer> categories) {
        this.id = id;
        this.s3Key = s3Key;
        this.description = description;
        this.name = name;
        this.leadsheetKey = leadsheetKey;
        this.categories = categories;
    }

    public EditPostDTO() {
    }

    @Nullable
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(@Nullable String s3Key) {
        this.s3Key = s3Key;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getLeadsheetKey() {
        return leadsheetKey;
    }

    public void setLeadsheetKey(@Nullable String leadsheetKey) {
        this.leadsheetKey = leadsheetKey;
    }

    @Nullable
    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(@Nullable List<Integer> categories) {
        this.categories = categories;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
