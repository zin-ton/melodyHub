package com.backend.melodyHub.dto;

public class AddPostDTO {
    private PostDTO postDTO;
    private String token;

    public AddPostDTO(PostDTO postDTO, String token) {
        this.postDTO = postDTO;
        this.token = token;
    }

    public PostDTO getPostDTO() {
        return postDTO;
    }

    public void setPostDTO(PostDTO postDTO) {
        this.postDTO = postDTO;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
