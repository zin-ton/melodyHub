package com.backend.melodyHub.dto;

public class EditPostDTO {
    private String token;
    private PostDTO post;

    public EditPostDTO(String token, PostDTO post) {
        this.token = token;
        this.post = post;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PostDTO getPostDTO() {
        return post;
    }

    public void setPost(PostDTO post) {
        this.post = post;
    }
}
