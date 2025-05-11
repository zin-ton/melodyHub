package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;

import java.util.List;
import java.util.Set;

public class PostDTO {
    private Integer id;
    private String s3Key;
    private String description;
    private String name;
    private byte[] leadsheet;
    private List<Integer> categories;

    public PostDTO(Integer id, String s3Key, String description, String name, byte[] leadsheet, List<Integer> categories) {
        this.id = id;
        this.s3Key = s3Key;
        this.description = description;
        this.name = name;
        this.leadsheet = leadsheet;
        this.categories = categories;
    }

    public Post toPost(User user, Set<Category> categories) {
        Post post = new Post();
        post.setS3Key(this.s3Key);
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
