package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Like;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByUserAndPost(User user, Post post);
}
