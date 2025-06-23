package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.PasswordHasher;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.LoginDTO;
import com.backend.melodyHub.dto.UserLoggedInDTO;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.dto.UserDTO;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Tag(name = "Auth Controller")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginRequest, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        else{
            try{
                Optional<User> userFromDb = userRepository.findByLogin(loginRequest.getLogin());
                if (userFromDb.isEmpty()) { return ResponseEntity.badRequest().body("username or password is not correct");}
                if(PasswordHasher.checkPassword(loginRequest.getPassword(), userFromDb.get().getPassword())) {
                    UserLoggedInDTO user = new UserLoggedInDTO(jwtUtil.generateToken(userFromDb.get()), userFromDb.get().getLogin());
                    return ResponseEntity.ok(user);
                }
                return ResponseEntity.badRequest().body("username or password is not correct");
            } catch (Exception e) {
                logger.error("An error occurred during login", e);
                return ResponseEntity.internalServerError().body("An error occurred during login");
            }
        }

    }

    @PostMapping("register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO user, BindingResult bindingResult) {
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
                    User newUser = new User();
                    newUser.setPassword(PasswordHasher.hashPassword(user.getPassword()));
                    newUser.setEmail(user.getEmail());
                    newUser.setLogin(user.getLogin());
                    newUser.setFirstName(user.getFirstName());
                    newUser.setLastName(user.getLastName());
                    userRepository.save(newUser);
                    return ResponseEntity.ok().build();
                }
            }
            catch (Exception e) {
                logger.error("An error occurred during registration", e);
                return ResponseEntity.internalServerError().body("An error occurred. Please try again later.");
            }
        }
    }

    @PostMapping("checkPassword")
    public ResponseEntity<?> checkPassword(@RequestHeader String token, @RequestBody String password) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        Optional<User> opt_user = userRepository.findByLogin(username);
        if(opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        else{
            User user = opt_user.get();
            if(!PasswordHasher.checkPassword(password, user.getPassword())) return ResponseEntity.ok(Boolean.FALSE);
            else return ResponseEntity.ok(Boolean.TRUE);
        }
    }
}
