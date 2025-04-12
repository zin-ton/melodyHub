package com.backend.melodyHub.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
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

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "leadsheet")
    private byte[] leadsheet;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_to_categories",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public byte[] getLeadsheet() {
        return leadsheet;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeadsheet(byte[] leadsheet) {
        this.leadsheet = leadsheet;
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
}
