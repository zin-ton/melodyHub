package com.backend.melodyHub.dto;

public class DeletePostDTO {
    private String token;
    private int postId;

    public DeletePostDTO(String token, int postId) {
        this.token = token;
        this.postId = postId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }
}
