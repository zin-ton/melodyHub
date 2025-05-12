package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.Saved;
import com.backend.melodyHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedRepository extends JpaRepository<Saved, Integer> {
    Optional<Saved> findByUserAndPost(User user, Post post);

    List<Saved> findByUser(User user);
}
