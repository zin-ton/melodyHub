package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/users")
    public List<User> retrieveAllBooks() {
        return userRepository.findAll();
    }

    @GetMapping("/userById")
    public ResponseEntity<?> getUserById(
            @RequestParam @NotNull(message = "Id cannot be null") Integer id,
            @RequestHeader
            @NotBlank(message = "Token cannot be empty")
            @Pattern(regexp = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$", message = "Invalid JWT token format") String token) {

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
