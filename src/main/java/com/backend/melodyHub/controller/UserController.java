package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.PasswordHasher;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.UpdatePasswordDTO;
import com.backend.melodyHub.dto.UserInfoDTO;
import com.backend.melodyHub.dto.UserNoPasswordDTO;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.CommentRepository;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@Tag(name = "User Controller")
public class UserController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CommentRepository commentRepository;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);


    public UserController(UserRepository userRepository, JwtUtil jwtUtil, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/userById")
    public ResponseEntity<?> getUserById(@RequestParam Integer id, @RequestHeader String token) {

        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));

        if (id == null) return ResponseEntity.badRequest().body("Id cannot be null");

        return userRepository.findById(id).map(UserNoPasswordDTO::fromUser).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/userByLogin")
    public ResponseEntity<?> getUserByLogin(@RequestParam String login, @RequestHeader String token) {

        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));

        if (login == null || login.isBlank()) return ResponseEntity.badRequest().body("Login cannot be empty");

        String loginRegex = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";
        if (!login.matches(loginRegex))
            return ResponseEntity.badRequest().body("Username must start with a letter and contain only letters, numbers, and underscores (3-20 characters, no dots allowed)");

        return userRepository.findByLogin(login).map(UserNoPasswordDTO::fromUser).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/editUser")
    public ResponseEntity<?> editUser(@RequestHeader String token, @RequestBody @Valid UserNoPasswordDTO userNoPasswordDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        Optional<User> user = userRepository.findByLogin(jwtUtil.extractUsername(token));
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        Optional<User> verifyLogin = userRepository.findByLogin(userNoPasswordDTO.getLogin());
        Optional<User> verifyEmail = userRepository.findByEmail(userNoPasswordDTO.getEmail());
        if (verifyLogin.isPresent() && !Objects.equals(user.get().getId(), verifyLogin.get().getId()))
            return ResponseEntity.badRequest().body("This login is already used");
        if (verifyEmail.isPresent() && !Objects.equals(user.get().getId(), verifyEmail.get().getId()))
            return ResponseEntity.badRequest().body("This email is already used");
        User userToEdit = user.get();
        userToEdit.setEmail(userNoPasswordDTO.getEmail());
        userToEdit.setFirstName(userNoPasswordDTO.getFirstName());
        userToEdit.setLastName(userNoPasswordDTO.getLastName());
        userToEdit.setLogin(userNoPasswordDTO.getLogin());
        try {
            userRepository.save(userToEdit);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<?> deleteUser(@RequestHeader String token, @RequestHeader String password) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,20}$";
        if (!password.matches(regex))
            return ResponseEntity.badRequest().body("Password must be 8-20 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)");
        String username = jwtUtil.extractUsername(token);
        Optional<User> opt_user = userRepository.findByLogin(username);
        if (opt_user.isEmpty()) return ResponseEntity.notFound().build();
        User user = opt_user.get();
        if (!PasswordHasher.checkPassword(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Password is not correct");
        }
        try {
            commentRepository.reassignCommentsToDeletedUser(user);
            userRepository.delete(user);
            userRepository.flush();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }
    }

    @GetMapping("getUserInfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if(!result.isValid())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("invalid token");
        try{
            Optional<User> opt_user = userRepository.findByLogin(jwtUtil.extractUsername(token));
            if(!opt_user.isPresent())
                return ResponseEntity.notFound().build();
            User user = opt_user.get();
            UserInfoDTO userInfo = new UserInfoDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getLogin());
            return ResponseEntity.ok(userInfo);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }
    }

    @PostMapping("updatePassword")
    public ResponseEntity<?> updatePassword(@RequestHeader String token, @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO){
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if(!result.isValid())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("invalid token");
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            if(opt_user.isEmpty()) return ResponseEntity.notFound().build();

            User user = opt_user.get();
            if(!PasswordHasher.checkPassword(updatePasswordDTO.getOldPassword(), user.getPassword())){
                return ResponseEntity.badRequest().body("Old password is incorrect");
            }
            String newPassword = updatePasswordDTO.getNewPassword();
            user.setPassword(PasswordHasher.hashPassword(newPassword));
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
            catch(Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }

    }

}
