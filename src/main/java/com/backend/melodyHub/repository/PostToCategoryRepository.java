package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.PostToCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostToCategoryRepository extends JpaRepository<PostToCategory, Integer> {
    // This interface can be extended with custom query methods if needed
}
