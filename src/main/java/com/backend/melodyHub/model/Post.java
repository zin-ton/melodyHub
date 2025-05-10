package com.backend.melodyHub.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "\"post\"")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "video_key")
    private String videoKey;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "leadsheet")
    private byte[] leadsheet;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "preview_key")
    private String previewKey;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "post_to_categories", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    public String getPreviewKey() {
        return previewKey;
    }

    public void setPreviewKey(String previewKey) {
        this.previewKey = previewKey;
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }
}
