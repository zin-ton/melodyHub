package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
