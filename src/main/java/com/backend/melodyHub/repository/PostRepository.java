package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findPostsByCategories(Set<Category> categories);
}
