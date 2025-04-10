package com.backend.melodyHub.controller;

import com.backend.melodyHub.configs.GlobalVariables;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.UserRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@CrossOrigin(origins = GlobalVariables.FRONTREND)
@RestController
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<User> retrieveAllBooks() {
        return userRepository.findAll();
    }

}
