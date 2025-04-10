package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.dto.UserNoPasswordDTO;
import com.backend.melodyHub.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/userById")
    public ResponseEntity<?> getUserDTOById(@RequestParam @NotNull(message = "Id cannot be null") Integer id, @RequestHeader @NotBlank(message = "Token cannot be empty") @Pattern(regexp = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$", message = "Invalid JWT token format") String token) {

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        return userRepository.findById(id).map(UserNoPasswordDTO::fromUser).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/userByLogin")
    public ResponseEntity<?> getUserByLogin(@RequestParam @NotEmpty(message = "Login cannot be empty") @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$", message = "Username must start with a letter and contain only letters, numbers, and underscores (3-20 characters, no dots allowed)") String login, @RequestHeader @NotBlank(message = "Token cannot be empty") @Pattern(regexp = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$", message = "Invalid JWT token format") String token) {

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        return userRepository.findByLogin(login).map(UserNoPasswordDTO::fromUser).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
