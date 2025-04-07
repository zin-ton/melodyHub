package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Validated
    Optional<User> findByLogin(String login);
    @Validated
    Optional<User> findByEmail(@NotBlank(message = "Email cannot be empty") @Email(message = "Invalid email format") String email);
}
