package com.backend.melodyHub.tests;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.PasswordHasher;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.controller.AuthController;
import com.backend.melodyHub.dto.LoginDTO;
import com.backend.melodyHub.dto.UserDTO;
import com.backend.melodyHub.dto.UserLoggedInDTO;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private BindingResult bindingResult;
    private AutoCloseable closeable;


    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void login_Success() {
        LoginDTO loginRequest = new LoginDTO();
        loginRequest.setPassword("password123");
        loginRequest.setLogin("testuser");
        User user = new User();
        user.setLogin("testuser");
        user.setPassword(PasswordHasher.hashPassword("password123"));
        String token = "mocked-jwt-token";

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class))).thenReturn(token);

        ResponseEntity<?> response = authController.login(loginRequest, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserLoggedInDTO expectedBody = new UserLoggedInDTO(token, "testuser");
        assertEquals(expectedBody, response.getBody());

        verify(userRepository, times(1)).findByLogin("testuser");
        verify(jwtUtil, times(1)).generateToken(any(User.class));
    }

    @Test
    void login_UserNotFound() {
        LoginDTO loginRequest = new LoginDTO();
        loginRequest.setLogin("nonexistent");
        loginRequest.setPassword("password123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.login(loginRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("username or password is not correct", response.getBody());

        verify(userRepository, times(1)).findByLogin("nonexistent");
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void login_IncorrectPassword() {
        LoginDTO loginRequest = new LoginDTO();
        loginRequest.setPassword("wrongpassword123");
        loginRequest.setLogin("testuser");
        User user = new User();
        user.setLogin("testuser");
        user.setPassword(PasswordHasher.hashPassword("password123"));

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.login(loginRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("username or password is not correct", response.getBody());

        verify(userRepository, times(1)).findByLogin("testuser");
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void login_BindingResultErrors() {
        LoginDTO loginRequest = new LoginDTO();
        loginRequest.setLogin("");
        loginRequest.setPassword("");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("loginDTO", "Login cannot be empty"),
                new ObjectError("loginDTO", "Password cannot be empty")
        ));

        ResponseEntity<?> response = authController.login(loginRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, ((List<?>) response.getBody()).size());

        verify(userRepository, never()).findByLogin(anyString());
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void login_InternalServerError() {
        LoginDTO loginRequest = new LoginDTO();
        loginRequest.setPassword("password123");
        loginRequest.setLogin("testuser");


        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(anyString())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = authController.login(loginRequest, bindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred during login", response.getBody());
    }

    @Test
    void register_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password123");
        userDTO.setLogin("newuser");
        userDTO.setEmail("new@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        User newUser = new User();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        ResponseEntity<?> response = authController.register(userDTO, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(userRepository, times(1)).findByLogin("newuser");
        verify(userRepository, times(1)).findByEmail("new@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UserAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password123");
        userDTO.setLogin("existinguser");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("new@example.com");
        User existingUser = new User();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin("existinguser")).thenReturn(Optional.of(existingUser));

        ResponseEntity<?> response = authController.register(userDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("user already exists!", response.getBody());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {

        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password123");
        userDTO.setLogin("newuser");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("existing@example.com");

        User existingUserWithEmail = new User();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUserWithEmail));

        ResponseEntity<?> response = authController.register(userDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("email already exists!", response.getBody());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_BindingResultErrors() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("");
        userDTO.setLogin("");
        userDTO.setFirstName("");
        userDTO.setLastName("");
        userDTO.setEmail("");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new ObjectError("userDTO", "Login cannot be empty"),
                new ObjectError("userDTO", "Email cannot be empty")
        ));

        ResponseEntity<?> response = authController.register(userDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, ((List<?>) response.getBody()).size());

        verify(userRepository, never()).findByLogin(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_InternalServerError() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password123");
        userDTO.setLogin("newuser");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("new@example.com");


        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database save error"));

        ResponseEntity<?> response = authController.register(userDTO, bindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred. Please try again later.", response.getBody());
    }

    @Test
    void checkPassword_Success_True() {
        String token = "valid-jwt-token";
        String password = "correctpassword";
        User user = new User();
        user.setLogin("testuser");
        user.setPassword(PasswordHasher.hashPassword("correctpassword"));

        when(jwtUtil.validateTokenFull(token)).thenReturn(new TokenValidationResult(true, ""));
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.checkPassword(token, password);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(token);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(userRepository, times(1)).findByLogin("testuser");
    }

    @Test
    void checkPassword_Success_False() {
        String token = "valid-jwt-token";
        String password = "wrongpassword";
        User user = new User();
        user.setLogin("testuser");
        user.setPassword(PasswordHasher.hashPassword("correctpassword"));

        when(jwtUtil.validateTokenFull(token)).thenReturn(new TokenValidationResult(true, ""));
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.checkPassword(token, password);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(token);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(userRepository, times(1)).findByLogin("testuser");
    }

    @Test
    void checkPassword_InvalidToken() {
        String token = "invalid-token";
        String password = "anypassword";

        when(jwtUtil.validateTokenFull(token)).thenReturn(new TokenValidationResult(false, "Invalid token message"));

        ResponseEntity<?> response = authController.checkPassword(token, password);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token message", response.getBody());

        verify(jwtUtil, never()).extractUsername(anyString());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void checkPassword_UserNotFoundForValidToken() {

        String token = "valid-token-for-nonexistent-user";
        String password = "anypassword";

        when(jwtUtil.validateTokenFull(token)).thenReturn(new TokenValidationResult(true, ""));
        when(jwtUtil.extractUsername(token)).thenReturn("nonexistentuser");
        when(userRepository.findByLogin("nonexistentuser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.checkPassword(token, password);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(token);
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(userRepository, times(1)).findByLogin("nonexistentuser");
    }
}
