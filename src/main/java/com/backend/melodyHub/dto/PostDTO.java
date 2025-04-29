package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;

import java.util.List;
import java.util.Set;

public class PostDTO {
    private Integer id;
    private String sourceUrl;
    private String description;
    private String name;
    private byte[] leadsheet;
    private List<Integer> categories;

    public PostDTO(Integer id, String sourceUrl, String description, String name, byte[] leadsheet, List<Integer> categories) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.description = description;
        this.name = name;
        this.leadsheet = leadsheet;
        this.categories = categories;
    }
    public static PostDTO fromPost(Post post) {
        return new PostDTO(
                post.getId(),
                post.getSourceUrl(),
                post.getDescription(),
                post.getName(),
                post.getLeadsheet(),
                post.getCategories().stream().map(Category::getId).toList()
        );
    }

    public Post toPost(User user, Set<Category> categories) {
        Post post = new Post();
        post.setSourceUrl(this.sourceUrl);
        post.setDescription(this.description);
        post.setName(this.name);
        post.setLeadsheet(this.leadsheet);
        post.setUser(user);
        post.setCategories(categories);
        return post;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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

    public byte[] getLeadsheet() {
        return leadsheet;
    }

    public void setLeadsheet(byte[] leadsheet) {
        this.leadsheet = leadsheet;
    }


    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }

}
