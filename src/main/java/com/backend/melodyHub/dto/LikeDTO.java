package com.backend.melodyHub.dto;


public class LikeDTO {
    private Integer likesOnPost;

    public LikeDTO(Integer likesOnPost) {
        this.likesOnPost = likesOnPost;
    }

    public Integer getLikesOnPost() {
        return likesOnPost;
    }

    public void setLikesOnPost(Integer likesOnPost) {
        this.likesOnPost = likesOnPost;
    }
}