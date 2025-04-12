package com.backend.melodyHub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {
    private Integer id;
    private String sourceUrl;
    private String description;
    private String name;
    private byte[] leadsheet;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;
    private List<String> categories;
    private String userName;

    public PostDTO(Integer id, String sourceUrl, String description, String name, byte[] leadsheet, LocalDateTime dateTime, List<String> categories, String userName) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.description = description;
        this.name = name;
        this.leadsheet = leadsheet;
        this.dateTime = dateTime;
        this.categories = categories;
        this.userName = userName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getLeadsheet() {
        return leadsheet;
    }

    public void setLeadsheet(byte[] leadsheet) {
        this.leadsheet = leadsheet;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
