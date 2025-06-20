package com.backend.melodyHub.dto;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OtherUserInfoDTO that = (OtherUserInfoDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, url);
    }

}
