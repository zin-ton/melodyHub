package com.backend.melodyHub.tests;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.controller.LikeController;
import com.backend.melodyHub.dto.LikeDTO;
import com.backend.melodyHub.model.Like;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.LikeRepository;
import com.backend.melodyHub.repository.PostRepository;
import com.backend.melodyHub.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LikeControllerTest {

    @InjectMocks
    private LikeController likeController;

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private LikeRepository likeRepository;

    private final String VALID_TOKEN = "valid-token";
    private final String INVALID_TOKEN = "invalid-token";
    private final String TEST_USERNAME = "testuser";
    private final Integer TEST_POST_ID = 1;
    private User testUser;
    private Post testPost;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1);
        testUser.setLogin(TEST_USERNAME);
        testUser.setEmail("user@example.com");

        testPost = new Post();
        testPost.setId(TEST_POST_ID);
        testPost.setName("Test Post");
        testPost.setUser(testUser);

        when(jwtUtil.validateTokenFull(VALID_TOKEN)).thenReturn(new TokenValidationResult(true, "Invalid token error"));
        when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn(TEST_USERNAME);

        when(jwtUtil.validateTokenFull(INVALID_TOKEN)).thenReturn(new TokenValidationResult(false, "Invalid token error"));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void likePost_Success() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.likePost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUsername(VALID_TOKEN);
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    void likePost_InvalidToken() {
        ResponseEntity<?> response = likeController.likePost(INVALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token error", response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(INVALID_TOKEN);
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(userRepository, never()).findByLogin(anyString());
        verify(postRepository, never()).findById(anyInt());
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void likePost_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.likePost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUsername(VALID_TOKEN);
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(postRepository, never()).findById(anyInt());
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void likePost_PostNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.likePost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUsername(VALID_TOKEN);
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void likePost_LikeAlreadyExists() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(new Like()));

        ResponseEntity<?> response = likeController.likePost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Like already exists", response.getBody());

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void likePost_InternalServerError() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Database error")).when(likeRepository).save(any(Like.class));

        ResponseEntity<?> response = likeController.likePost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody());

        verify(likeRepository, times(1)).save(any(Like.class));
    }


    @Test
    void deleteLike_Success() {
        Like existingLike = new Like();
        existingLike.setUser(testUser);
        existingLike.setPost(testPost);

        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(existingLike));

        ResponseEntity<?> response = likeController.deleteLike(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUsername(VALID_TOKEN);
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
        verify(likeRepository, times(1)).delete(existingLike);
    }

    @Test
    void deleteLike_InvalidToken() {
        ResponseEntity<?> response = likeController.deleteLike(INVALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token error", response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(INVALID_TOKEN);
        verify(userRepository, never()).findByLogin(anyString());
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void deleteLike_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.deleteLike(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void deleteLike_PostNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.deleteLike(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void deleteLike_LikeNotExists() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.deleteLike(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Like not exists", response.getBody());

        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    void deleteLike_InternalServerError() {
        Like existingLike = new Like();
        existingLike.setUser(testUser);
        existingLike.setPost(testPost);

        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(existingLike));
        doThrow(new RuntimeException("Database error")).when(likeRepository).delete(any(Like.class));

        ResponseEntity<?> response = likeController.deleteLike(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody());

        verify(likeRepository, times(1)).delete(existingLike);
    }


    @Test
    void getLikesOnPost_Success() {
        Integer likeCount = 5;
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.countLikesByPost(testPost)).thenReturn(likeCount);

        ResponseEntity<?> response = likeController.getLikesOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new LikeDTO(likeCount), response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, times(1)).countLikesByPost(testPost);
    }

    @Test
    void getLikesOnPost_InvalidToken() {
        ResponseEntity<?> response = likeController.getLikesOnPost(INVALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token error", response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(INVALID_TOKEN);
        verify(postRepository, never()).findById(anyInt());
        verify(likeRepository, never()).countLikesByPost(any(Post.class));
    }

    @Test
    void getLikesOnPost_PostNotFound() {
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.getLikesOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, never()).countLikesByPost(any(Post.class));
    }

    @Test
    void getLikesOnPost_InternalServerError() {
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        doThrow(new RuntimeException("Database error")).when(likeRepository).countLikesByPost(any(Post.class));

        ResponseEntity<?> response = likeController.getLikesOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("something went wrong", response.getBody());

        verify(likeRepository, times(1)).countLikesByPost(testPost);
    }

    @Test
    void checkLikeOnPost_ReturnsTrue_WhenLikeExists() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.of(new Like()));

        ResponseEntity<?> response = likeController.checkLikeOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUsername(VALID_TOKEN);
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
    }

    @Test
    void checkLikeOnPost_ReturnsFalse_WhenLikeDoesNotExist() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByUserAndPost(testUser, testPost)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.checkLikeOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(VALID_TOKEN);
        verify(jwtUtil, times(1)).extractUsername(VALID_TOKEN);
        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
    }

    @Test
    void checkLikeOnPost_InvalidToken() {
        ResponseEntity<?> response = likeController.checkLikeOnPost(INVALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token error", response.getBody());

        verify(jwtUtil, times(1)).validateTokenFull(INVALID_TOKEN);
        verify(userRepository, never()).findByLogin(anyString());
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
    }

    @Test
    void checkLikeOnPost_UserNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.checkLikeOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(userRepository, times(1)).findByLogin(TEST_USERNAME);
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
    }

    @Test
    void checkLikeOnPost_PostNotFound() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<?> response = likeController.checkLikeOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(postRepository, times(1)).findById(TEST_POST_ID);
        verify(likeRepository, never()).findByUserAndPost(any(User.class), any(Post.class));
    }

    @Test
    void checkLikeOnPost_InternalServerError() {
        when(userRepository.findByLogin(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        doThrow(new RuntimeException("Database error")).when(likeRepository).findByUserAndPost(any(User.class), any(Post.class));

        ResponseEntity<?> response = likeController.checkLikeOnPost(VALID_TOKEN, TEST_POST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("something went wrong", response.getBody());

        verify(likeRepository, times(1)).findByUserAndPost(testUser, testPost);
    }
}
