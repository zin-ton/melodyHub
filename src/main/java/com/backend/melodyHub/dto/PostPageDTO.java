package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.PostToCategory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostPageDTO {
    private final Integer id;
    private final String previewUrl;
    private final String authorName;
    private List<Integer> categories;
    private final String description;
    private final String postUrl;
    private final String name;
    private final String leadsheetUrl;

    public PostPageDTO(Integer id, String previewUrl, String authorName, List<Integer> categories, String description, String postUrl, String name, String leadsheetUrl) {
        this.id = id;
        this.previewUrl = previewUrl;
        this.authorName = authorName;
        this.categories = categories;
        this.description = description;
        this.postUrl = postUrl;
        this.name = name;
        this.leadsheetUrl = leadsheetUrl;
    }

    public static PostPageDTO fromPost(Post post, String previewUrl, String postUrl, String leadsheetUrl) {
        List<Integer> categoryIds = post.getPostToCategories().stream()
                .map(PostToCategory::getCategory)
                .map(category -> category.getId())
                .collect(Collectors.toList());

        return new PostPageDTO(
                post.getId(),
                previewUrl,
                Optional.ofNullable(post.getUser().getLogin()).orElse(""),
                categoryIds,
                post.getDescription(),
                postUrl,
                post.getName(),
                leadsheetUrl
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

    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public String getName() {
        return name;
    }

    public String getLeadsheetUrl() {
        return leadsheetUrl;
    }
}