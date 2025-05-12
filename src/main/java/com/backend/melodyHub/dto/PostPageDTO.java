package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;

import java.util.List;
import java.util.Optional;

public class PostPageDTO {
    private final Integer id;
    private final String previewUrl;
    private final String authorName;
    private List<Integer> categories;
    private final String description;
    private final String postUrl;
    private final String name;

    public PostPageDTO(Integer id, String previewUrl, String authorName, List<Integer> categories, String description, String postUrl, String name) {
        this.id = id;
        this.previewUrl = previewUrl;
        this.authorName = authorName;
        this.categories = categories;
        this.description = description;
        this.postUrl = postUrl;
        this.name = name;
    }

    public static PostPageDTO fromPost(Post post, String previewUrl, String postUrl) {
        return new PostPageDTO(post.getId(), previewUrl, String.valueOf(Optional.ofNullable(post.getUser().getLogin())), post.getCategories().stream().map(Category::getId).toList(), post.getDescription(), postUrl, post.getName());
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
}
