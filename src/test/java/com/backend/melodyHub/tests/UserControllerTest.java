package com.backend.melodyHub.tests;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.PasswordHasher;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.controller.UserController;
import com.backend.melodyHub.dto.OtherUserInfoDTO;
import com.backend.melodyHub.dto.UpdatePasswordDTO;
import com.backend.melodyHub.dto.UserInfoDTO;
import com.backend.melodyHub.dto.UserNoPasswordDTO;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.CommentRepository;
import com.backend.melodyHub.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private final String VALID_TOKEN = "valid-token";
    private final String INVALID_TOKEN = "invalid-token";
    private final String TEST_USERNAME = "testuser";
    @InjectMocks
    private UserController userController;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private BindingResult bindingResult;
    private User testUser;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1);
        testUser.setLogin(TEST_USERNAME);
        String TEST_EMAIL = "test@example.com";
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setS3Key("s3key123");

        when(jwtUtil.validateTokenFull(VALID_TOKEN)).thenReturn(new TokenValidationResult(true, ""));
        when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);

        when(jwtUtil.validateTokenFull(INVALID_TOKEN)).thenReturn(new TokenValidationResult(false, "Invalid token"));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = userController.getUserById(1, VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UserNoPasswordDTO.fromUser(testUser), response.getBody());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void getUserById_UserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById(99, VALID_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findById(99);
    }

    @Test
    void getUserById_InvalidToken() {
        ResponseEntity<?> response = userController.getUserById(1, INVALID_TOKEN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserById_NullId() {
        ResponseEntity<?> response = userController.getUserById(null, VALID_TOKEN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Id cannot be null", response.getBody());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserByLogin_Success() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = userController.getUserByLogin(TEST_USERNAME, VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UserNoPasswordDTO.fromUser(testUser), response.getBody());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
    }

    @Test
    void getUserByLogin_UserNotFound() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserByLogin("nonexistent", VALID_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin("nonexistent");
    }

    @Test
    void getUserByLogin_InvalidToken() {
        ResponseEntity<?> response = userController.getUserByLogin(TEST_USERNAME, INVALID_TOKEN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void getUserByLogin_NullLogin() {
        ResponseEntity<?> response = userController.getUserByLogin(null, VALID_TOKEN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Login cannot be empty", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void getUserByLogin_BlankLogin() {
        ResponseEntity<?> response = userController.getUserByLogin("  ", VALID_TOKEN);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Login cannot be empty", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void getUserByLogin_InvalidLoginFormat() {
        ResponseEntity<?> response = userController.getUserByLogin("1_invalid", VALID_TOKEN);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username must start with a letter and contain only letters, numbers, and underscores (3-20 characters, no dots allowed)", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void editUser_Success() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("updatedLogin");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("User");


        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByLogin("updatedLogin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<?> response = userController.editUser(VALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, times(1)).findByLogin("updatedLogin");
        verify(userRepository, times(1)).findByEmail("updated@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals("updatedLogin", testUser.getLogin());
    }

    @Test
    void editUser_BindingResultErrors() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("");
        updateDTO.setEmail("");
        updateDTO.setFirstName("");
        updateDTO.setLastName("");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("user", "Login cannot be empty")));

        ResponseEntity<?> response = userController.editUser(VALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, ((List<?>) response.getBody()).size());
        verify(jwtUtil, never()).validateTokenFull(anyString());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void editUser_InvalidToken() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("updatedLogin");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("User");

        ResponseEntity<?> response = userController.editUser(INVALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void editUser_UserNotFoundAfterToken() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("updatedLogin");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("User");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.editUser(VALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void editUser_LoginAlreadyUsedByOtherUser() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("existingLogin");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("User");
        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setLogin("existingLogin");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByLogin("existingLogin")).thenReturn(Optional.of(anotherUser));

        ResponseEntity<?> response = userController.editUser(VALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This login is already used", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void editUser_EmailAlreadyUsedByOtherUser() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("newLogin");
        updateDTO.setEmail("existing@example.com");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("User");
        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setEmail("existing@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByLogin("newLogin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(anotherUser));

        ResponseEntity<?> response = userController.editUser(VALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This email is already used", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void editUser_InternalServerError() {
        UserNoPasswordDTO updateDTO = new UserNoPasswordDTO();
        updateDTO.setId(1);
        updateDTO.setLogin("updatedLogin");
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("User");
        updateDTO.setEmail("updated@example.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByLogin("updatedLogin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = userController.editUser(VALID_TOKEN, updateDTO, bindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred.", response.getBody());
    }

    @Test
    void deleteUser_Success() {
        String correctPassword = "CorrectPassword1!";
        String hashedPassword = "hashedPassword";
        testUser.setPassword(hashedPassword);

        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            mockedPasswordHasher.when(() -> PasswordHasher.checkPassword(correctPassword, hashedPassword)).thenReturn(true);

            when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            doNothing().when(commentRepository).reassignCommentsToDeletedUser(any(User.class));
            doNothing().when(userRepository).delete(any(User.class));
            doNothing().when(userRepository).flush();

            ResponseEntity<?> response = userController.deleteUser(VALID_TOKEN, correctPassword);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
            verify(commentRepository, times(1)).reassignCommentsToDeletedUser(testUser);
            verify(userRepository, times(1)).delete(testUser);
            verify(userRepository, times(1)).flush();
        }
    }

    @Test
    void deleteUser_InvalidToken() {
        ResponseEntity<?> response = userController.deleteUser(INVALID_TOKEN, "AnyPassword1!");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void deleteUser_InvalidPasswordFormat() {
        ResponseEntity<?> response = userController.deleteUser(VALID_TOKEN, "short");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password must be 8-20 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.deleteUser(VALID_TOKEN, "ValidPassword1!");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(commentRepository, never()).reassignCommentsToDeletedUser(any(User.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_IncorrectPassword() {
        String providedPassword = "WrongPassword1!";
        String hashedPassword = "hashedPassword";
        testUser.setPassword(hashedPassword);

        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            mockedPasswordHasher.when(() -> PasswordHasher.checkPassword(providedPassword, hashedPassword)).thenReturn(false);

            when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));

            ResponseEntity<?> response = userController.deleteUser(VALID_TOKEN, providedPassword);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Password is not correct", response.getBody());
            verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
            verify(commentRepository, never()).reassignCommentsToDeletedUser(any(User.class));
            verify(userRepository, never()).delete(any(User.class));
        }
    }

    @Test
    void deleteUser_InternalServerError() {
        String correctPassword = "CorrectPassword1!";
        String hashedPassword = "hashedPassword";
        testUser.setPassword(hashedPassword);

        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            mockedPasswordHasher.when(() -> PasswordHasher.checkPassword(correctPassword, hashedPassword)).thenReturn(true);

            when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            doThrow(new RuntimeException("DB error")).when(userRepository).delete(any(User.class));

            ResponseEntity<?> response = userController.deleteUser(VALID_TOKEN, correctPassword);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("Unexpected error occurred.", response.getBody());
            verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
            verify(userRepository, times(1)).delete(testUser);
        }
    }

    @Test
    void getUserInfo_Success() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(s3Service.generatePresignedImageUrl(testUser.getS3Key())).thenReturn("http://s3.url/key123");

        ResponseEntity<?> response = userController.getUserInfo(VALID_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserInfoDTO expectedBody = new UserInfoDTO(testUser.getId(), testUser.getEmail(), testUser.getFirstName(),
                testUser.getLastName(), testUser.getLogin(), "http://s3.url/key123");
        assertEquals(expectedBody, response.getBody());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(s3Service, times(1)).generatePresignedImageUrl(testUser.getS3Key());
    }

    @Test
    void getUserInfo_InvalidToken() {
        ResponseEntity<?> response = userController.getUserInfo(INVALID_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void getUserInfo_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserInfo(VALID_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(s3Service, never()).generatePresignedImageUrl(anyString());
    }

    @Test
    void getUserInfo_InternalServerError() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = userController.getUserInfo(VALID_TOKEN);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred.", response.getBody());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
    }

    @Test
    void updatePassword_Success() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("oldPass", "NewPassword1!");
        String oldHashedPass = "oldHashedPass";
        String newHashedPass = "newHashedPass";
        testUser.setPassword(oldHashedPass);

        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            mockedPasswordHasher.when(() -> PasswordHasher.checkPassword("oldPass", oldHashedPass)).thenReturn(true);
            mockedPasswordHasher.when(() -> PasswordHasher.hashPassword("NewPassword1!")).thenReturn(newHashedPass);

            when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            ResponseEntity<?> response = userController.updatePassword(VALID_TOKEN, updatePasswordDTO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
            verify(userRepository, times(1)).save(testUser);
            assertEquals(newHashedPass, testUser.getPassword());
        }
    }

    @Test
    void updatePassword_InvalidToken() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("oldPass", "NewPassword1!");

        ResponseEntity<?> response = userController.updatePassword(INVALID_TOKEN, updatePasswordDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void updatePassword_UserNotFound() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("oldPass", "NewPassword1!");

        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.updatePassword(VALID_TOKEN, updatePasswordDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePassword_IncorrectOldPassword() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("wrongOldPass", "NewPassword1!");
        String oldHashedPass = "oldHashedPass";
        testUser.setPassword(oldHashedPass);

        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            mockedPasswordHasher.when(() -> PasswordHasher.checkPassword("wrongOldPass", oldHashedPass)).thenReturn(false);

            when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));

            ResponseEntity<?> response = userController.updatePassword(VALID_TOKEN, updatePasswordDTO);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Old password is incorrect", response.getBody());
            verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void updatePassword_InternalServerError() {
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("oldPass", "NewPassword1!");
        String oldHashedPass = "oldHashedPass";
        testUser.setPassword(oldHashedPass);

        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            mockedPasswordHasher.when(() -> PasswordHasher.checkPassword("oldPass", oldHashedPass)).thenReturn(true);

            when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

            ResponseEntity<?> response = userController.updatePassword(VALID_TOKEN, updatePasswordDTO);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals("Unexpected error occurred.", response.getBody());
            verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
            verify(userRepository, times(1)).save(testUser);
        }
    }

    @Test
    void getOtherUserInfo_Success() {
        String otherUsername = "otheruser";
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setLogin(otherUsername);
        otherUser.setS3Key("otherS3Key");

        when(userRepository.findByLogin(otherUsername)).thenReturn(Optional.of(otherUser));
        when(s3Service.generatePresignedImageUrl(otherUser.getS3Key())).thenReturn("http://s3.url/otherkey");

        ResponseEntity<?> response = userController.getOtherUserInfo(VALID_TOKEN, otherUsername);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        OtherUserInfoDTO expectedBody = new OtherUserInfoDTO(otherUser.getId(), otherUser.getLogin(), "http://s3.url/otherkey");
        assertEquals(expectedBody, response.getBody());
        verify(userRepository, times(1)).findByLogin(otherUsername);
        verify(s3Service, times(1)).generatePresignedImageUrl(otherUser.getS3Key());
    }

    @Test
    void getOtherUserInfo_InvalidToken() {
        ResponseEntity<?> response = userController.getOtherUserInfo(INVALID_TOKEN, "someuser");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void getOtherUserInfo_UserNotFound() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getOtherUserInfo(VALID_TOKEN, "nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin("nonexistent");
        verify(s3Service, never()).generatePresignedImageUrl(anyString());
    }

    @Test
    void getOtherUserInfo_InternalServerError() {
        when(userRepository.findByLogin(anyString())).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = userController.getOtherUserInfo(VALID_TOKEN, "someuser");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred.", response.getBody());
        verify(userRepository, times(1)).findByLogin(anyString());
    }

    @Test
    void changeEmail_Success() {
        String newEmail = "new@example.com";
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<?> response = userController.changeEmail(VALID_TOKEN, newEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(userRepository, times(1)).save(testUser);
        assertEquals(newEmail, testUser.getEmail());
    }

    @Test
    void changeEmail_InvalidToken() {
        ResponseEntity<?> response = userController.changeEmail(INVALID_TOKEN, "new@example.com");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void changeEmail_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.changeEmail(VALID_TOKEN, "new@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeEmail_EmailAlreadyUsedByOtherUser() {
        String newEmail = "used@example.com";
        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setEmail(newEmail);

        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(anotherUser));

        ResponseEntity<?> response = userController.changeEmail(VALID_TOKEN, newEmail);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This email is already used", response.getBody());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeEmail_InternalServerError() {
        String newEmail = "new@example.com";
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = userController.changeEmail(VALID_TOKEN, newEmail);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred.", response.getBody());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void changeProfilePicture_Success() {
        String newS3Key = "newS3Key123";
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<?> response = userController.changeProfilePicture(VALID_TOKEN, newS3Key);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, times(1)).save(testUser);
        assertEquals(newS3Key, testUser.getS3Key());
    }

    @Test
    void changeProfilePicture_InvalidToken() {
        ResponseEntity<?> response = userController.changeProfilePicture(INVALID_TOKEN, "someKey");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("invalid token", response.getBody());
        verify(userRepository, never()).findByLogin(anyString());
    }

    @Test
    void changeProfilePicture_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.changeProfilePicture(VALID_TOKEN, "someKey");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeProfilePicture_InternalServerError() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = userController.changeProfilePicture(VALID_TOKEN, "someKey");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred.", response.getBody());
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(userRepository, times(1)).save(testUser);
    }
}