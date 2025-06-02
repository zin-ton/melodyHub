package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT p FROM Post p JOIN p.categories c " +
            "WHERE c IN :categories " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT c.id) = :categoryCount")
    List<Post> findPostsWithAllCategories(@Param("categories") Set<Category> categories,
                                          @Param("categoryCount") long categoryCount);

    List<Post> getPostsByUser(User user);
}
