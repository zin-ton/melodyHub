package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.PasswordHasher;
import com.backend.melodyHub.model.LoginRequest;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
@CrossOrigin
@RestController
@Tag(name = "Auth Controller")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("login")
    @Operation()
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        else{
            try{
                Optional<User> userFromDb = userRepository.findByLogin(loginRequest.getLogin());
                if (userFromDb.isEmpty()) { return ResponseEntity.badRequest().body("username or password is not correct");}
                if(PasswordHasher.checkPassword(loginRequest.getPassword(), userFromDb.get().getPassword())) {
                    return ResponseEntity.ok(jwtUtil.generateToken(userFromDb.get()));
                }
                return ResponseEntity.badRequest().body("username or password is not correct");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

    }

    @PostMapping("register")
    @Operation()
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        else{
            try{
                Optional<User> userFromDb = userRepository.findByLogin(user.getLogin());
                Optional<User> verifyEmail = userRepository.findByEmail(user.getEmail());
                if (userFromDb.isPresent()) { return ResponseEntity.badRequest().body("user already exists!"); }
                else if(verifyEmail.isPresent()) { return ResponseEntity.badRequest().body("email already exists!"); }
                else{
                    user.setPassword(PasswordHasher.hashPassword(user.getPassword()));
                    userRepository.save(user);
                    return ResponseEntity.ok().build();
                }
            }
            catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

    }
}
