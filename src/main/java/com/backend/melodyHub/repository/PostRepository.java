package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.name = :name")
    List<Post> findPostsByCategoryName(@Param("name") String name);

}
