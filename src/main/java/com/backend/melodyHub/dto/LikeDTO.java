package com.backend.melodyHub.dto;


import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeDTO likeDTO = (LikeDTO) o;
        return Objects.equals(likesOnPost, likeDTO.likesOnPost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(likesOnPost);
    }

}