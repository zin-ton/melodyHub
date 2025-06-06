package com.backend.melodyHub.dto;

public class OtherUserInfoDTO {
    private Integer id;
    private String username;
    private String url;

    public OtherUserInfoDTO(Integer id, String username, String url) {
        this.id = id;
        this.username = username;
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
