package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.PostToCategory;
import com.backend.melodyHub.model.User;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostDTO {
    private Integer id;
    private String s3Key;
    private String description;
    private String name;
    private String leadsheetKey;
    private List<Integer> categories;
    private String author;
    @Nullable
    private LocalDateTime dateTime;

    public PostDTO(Integer id, String s3Key, String description, String name, String leadsheet, List<Integer> categories, String author, LocalDateTime dateTime) {
        this.id = id;
        this.s3Key = s3Key;
        this.description = description;
        this.name = name;
        this.leadsheetKey = leadsheet;
        this.categories = categories;
        this.author = author;
        this.dateTime = dateTime;
    }

    public Post toPost(User user) {
        Post post = new Post();
        post.setS3Key(this.s3Key);
        post.setDescription(this.description);
        post.setName(this.name);
        post.setLeadsheetKey(this.leadsheetKey);
        post.setUser(user);
        return post;
    }

    public static PostDTO fromPost(Post post) {
        List<Integer> categoryIds = post.getPostToCategories().stream()
                .map(PostToCategory::getCategory)
                .map(Category::getId)
                .collect(Collectors.toList());

        return new PostDTO(
                post.getId(),
                post.getS3Key(),
                post.getDescription(),
                post.getName(),
                post.getLeadsheetKey(),
                categoryIds,
                post.getUser().getLogin(),
                post.getDateTime()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeadsheetKey() {
        return leadsheetKey;
    }

    public void setLeadsheetKey(String leadsheetKey) {
        this.leadsheetKey = leadsheetKey;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
