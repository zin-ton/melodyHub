package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;

import java.util.List;
import java.util.Optional;

public class PostPreviewDTO {
    private final Integer id;
    private final String previewUrl;
    private final String authorName;
    private List<Integer> categories;

    public PostPreviewDTO(Integer id, String previewUrl, String authorName, List<Integer> categories) {
        this.id = id;
        this.previewUrl = previewUrl;
        this.authorName = authorName;
        this.categories = categories;
    }

    public static PostPreviewDTO fromPost(Post post, String previewUrl) {
        String fullName = Optional.ofNullable(post.getUser().getFirstName()).orElse("") + " " + Optional.ofNullable(post.getUser().getLastName()).orElse("");
        fullName = fullName.trim();
        return new PostPreviewDTO(post.getId(), previewUrl, fullName, post.getCategories().stream().map(Category::getId).toList());
    }

    public Integer getId() {
        return id;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }
}
