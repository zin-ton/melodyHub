package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.PostToCategory;

import java.util.List;
import java.util.stream.Collectors;

public class PostPreviewDTO {
    private final Integer id;
    private final String previewUrl;
    private final String name;
    private final String authorName;
    private List<Integer> categories;

    public PostPreviewDTO(Integer id, String previewUrl, String name, String authorName, List<Integer> categories) {
        this.id = id;
        this.previewUrl = previewUrl;
        this.name = name;
        this.authorName = authorName;
        this.categories = categories;
    }

    public static PostPreviewDTO fromPost(Post post, String previewUrl) {
        List<Integer> categoryIds = post.getPostToCategories().stream()
                .map(PostToCategory::getCategory)
                .map(category -> category.getId())
                .collect(Collectors.toList());

        return new PostPreviewDTO(
                post.getId(),
                previewUrl,
                post.getName(),
                post.getUser().getLogin(),
                categoryIds
        );
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

    public String getName() {
        return name;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }
}