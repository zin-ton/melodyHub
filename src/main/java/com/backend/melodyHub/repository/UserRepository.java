package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLogin(@NotEmpty(message = "Login cannot be empty") @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$", message = "Username must start with a letter and contain only letters, numbers, and underscores (3-20 characters, no dots allowed)") String login);

    Optional<User> findByEmail(@NotBlank(message = "Email cannot be empty") @Email(message = "Invalid email format") String email);
}
