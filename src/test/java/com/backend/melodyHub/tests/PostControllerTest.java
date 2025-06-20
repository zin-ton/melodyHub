package com.backend.melodyHub.tests;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.controller.PostController;
import com.backend.melodyHub.dto.AddPostDTO;
import com.backend.melodyHub.dto.EditPostDTO;
import com.backend.melodyHub.dto.PostPageDTO;
import com.backend.melodyHub.dto.PostPreviewDTO;
import com.backend.melodyHub.model.*;
import com.backend.melodyHub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PostControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SavedRepository savedRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostToCategoryRepository postToCategoryRepository;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeletePost_Success() {
        String token = "validToken";
        int postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        Post post = new Post();
        post.setId(postId);
        post.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ResponseEntity<?> response = postController.deletePost(token, postId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Post deleted successfully", response.getBody());
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void testDeletePost_InvalidToken() {
        String token = "invalidToken";
        int postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.deletePost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(postRepository, never()).delete(any());
    }

    @Test
    void testDeletePost_PostNotFound() {
        String token = "validToken";
        int postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.deletePost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
        verify(postRepository, never()).delete(any());
    }

    @Test
    void testDeletePost_UnauthorizedAccess() {
        String token = "validToken";
        int postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        User anotherUser = new User();
        anotherUser.setId(2);
        Post post = new Post();
        post.setId(postId);
        post.setUser(anotherUser);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ResponseEntity<?> response = postController.deletePost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("You are not the owner of this post", response.getBody());
        verify(postRepository, never()).delete(any());
    }

    @Test
    void testAddPost_InvalidToken() {
        String token = "invalidToken";
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");
        AddPostDTO postDTO = new AddPostDTO();

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.addPost(postDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testAddPost_UserNotFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        AddPostDTO postDTO = new AddPostDTO();

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.addPost(postDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testAddPost_InvalidCategory() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        AddPostDTO postDTO = new AddPostDTO();
        postDTO.setCategories(List.of(1, 999));

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(new Category()));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.addPost(postDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Category with id 999 does not exist", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testEditPost_Success() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(1);
        post.setUser(user);
        EditPostDTO editPostDTO = new EditPostDTO();
        editPostDTO.setId(1);
        editPostDTO.setName("Updated Post");
        editPostDTO.setCategories(List.of(1, 2));
        Category category1 = new Category();
        category1.setId(1);
        Category category2 = new Category();
        category2.setId(2);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category1));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(category2));

        ResponseEntity<?> response = postController.editPost(editPostDTO, token);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Post edited successfully", response.getBody());
        verify(postRepository, times(1)).save(post);
        verify(postToCategoryRepository, times(1)).deleteAll(post.getPostToCategories());
        verify(postToCategoryRepository, times(1)).saveAll(any());
    }

    @Test
    void testEditPost_InvalidToken() {
        String token = "invalidToken";
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");
        EditPostDTO editPostDTO = new EditPostDTO();

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.editPost(editPostDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testEditPost_UserNotFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        EditPostDTO editPostDTO = new EditPostDTO();

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.editPost(editPostDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testEditPost_PostNotFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        EditPostDTO editPostDTO = new EditPostDTO();
        editPostDTO.setId(999);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.editPost(editPostDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testEditPost_UnauthorizedAccess() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        User anotherUser = new User();
        anotherUser.setId(2);
        Post post = new Post();
        post.setId(1);
        post.setUser(anotherUser);
        EditPostDTO editPostDTO = new EditPostDTO();
        editPostDTO.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        ResponseEntity<?> response = postController.editPost(editPostDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("You are not the owner of this post", response.getBody());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testEditPost_InvalidCategory() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(1);
        post.setUser(user);
        EditPostDTO editPostDTO = new EditPostDTO();
        editPostDTO.setId(1);
        editPostDTO.setCategories(List.of(1, 999));

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(new Category()));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.editPost(editPostDTO, token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Category with id 999 does not exist", response.getBody());
        verify(postRepository, never()).save(any());
    }
    @Test
    void testGetPost_Success() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        user.setS3Key("userKey");
        Post post = new Post();
        post.setId(postId);
        post.setName("Test Post");
        post.setDescription("Test Description");
        post.setS3Key("postKey");
        post.setLeadsheetKey("leadsheetKey");
        post.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(s3Service.generatePresignedPreviewUrl("postKey")).thenReturn("previewUrl");
        when(s3Service.generatePresignedVideoUrl("postKey")).thenReturn("videoUrl");
        when(s3Service.generatePresignedLeadsheetUrl("leadsheetKey")).thenReturn("leadsheetUrl");
        when(s3Service.generatePresignedImageUrl("userKey")).thenReturn("imageUrl");

        ResponseEntity<?> response = postController.getPost(token, postId);

        assertEquals(200, response.getStatusCodeValue());
        PostPageDTO postPageDTO = (PostPageDTO) response.getBody();
        assertEquals("Test Post", postPageDTO.getName());
        assertEquals("Test Description", postPageDTO.getDescription());
        assertEquals("previewUrl", postPageDTO.getPreviewUrl());
        assertEquals("videoUrl", postPageDTO.getPostUrl());
        assertEquals("leadsheetUrl", postPageDTO.getLeadsheetUrl());
        assertEquals("imageUrl", postPageDTO.getAuthorProfileImageUrl());
    }

    @Test
    void testGetPost_InvalidToken() {
        String token = "invalidToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.getPost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testGetPost_PostNotFound() {
        String token = "validToken";
        Integer postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.getPost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
    }
    @Test
    void testAddPostToFavorites_Success() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(postId);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(savedRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.addPostToFavorites(token, postId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Post saved successfully", response.getBody());
        verify(savedRepository, times(1)).save(any());
    }

    @Test
    void testAddPostToFavorites_InvalidToken() {
        String token = "invalidToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.addPostToFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(savedRepository, never()).save(any());
    }

    @Test
    void testAddPostToFavorites_UserNotFound() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.addPostToFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
        verify(savedRepository, never()).save(any());
    }

    @Test
    void testAddPostToFavorites_PostNotFound() {
        String token = "validToken";
        Integer postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.addPostToFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
        verify(savedRepository, never()).save(any());
    }

    @Test
    void testAddPostToFavorites_PostAlreadySaved() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(postId);
        Saved saved = new Saved();
        saved.setUser(user);
        saved.setPost(post);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(savedRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(saved));

        ResponseEntity<?> response = postController.addPostToFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post already saved", response.getBody());
        verify(savedRepository, never()).save(any());
    }

    @Test
    void testDeletePostFromFavorites_Success() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(postId);
        Saved saved = new Saved();
        saved.setUser(user);
        saved.setPost(post);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(savedRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(saved));

        ResponseEntity<?> response = postController.deletePostFromFavorites(token, postId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Post removed from saved successfully", response.getBody());
        verify(savedRepository, times(1)).delete(saved);
    }

    @Test
    void testDeletePostFromFavorites_InvalidToken() {
        String token = "invalidToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.deletePostFromFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(savedRepository, never()).delete(any());
    }

    @Test
    void testDeletePostFromFavorites_UserNotFound() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.deletePostFromFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
        verify(savedRepository, never()).delete(any());
    }

    @Test
    void testDeletePostFromFavorites_PostNotFound() {
        String token = "validToken";
        Integer postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.deletePostFromFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
        verify(savedRepository, never()).delete(any());
    }

    @Test
    void testDeletePostFromFavorites_PostNotSaved() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(postId);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(savedRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.deletePostFromFavorites(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not saved", response.getBody());
        verify(savedRepository, never()).delete(any());
    }

    @Test
    void testGetSavedPosts_Success() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post1 = new Post();
        post1.setId(1);
        post1.setName("Post 1");
        post1.setS3Key("post1Key");
        post1.setUser(user); // Ensure the User field is set
        Post post2 = new Post();
        post2.setId(2);
        post2.setName("Post 2");
        post2.setS3Key("post2Key");
        post2.setUser(user); // Ensure the User field is set
        Saved saved1 = new Saved();
        saved1.setUser(user);
        saved1.setPost(post1);
        Saved saved2 = new Saved();
        saved2.setUser(user);
        saved2.setPost(post2);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(savedRepository.findByUser(user)).thenReturn(List.of(saved1, saved2));
        when(s3Service.generatePresignedPreviewUrl("post1Key")).thenReturn("previewUrl1");
        when(s3Service.generatePresignedPreviewUrl("post2Key")).thenReturn("previewUrl2");

        ResponseEntity<?> response = postController.getSavedPosts(token);

        assertEquals(200, response.getStatusCodeValue());
        List<PostPreviewDTO> posts = (List<PostPreviewDTO>) response.getBody();
        assertEquals(2, posts.size());
        assertEquals("Post 1", posts.get(0).getName());
        assertEquals("previewUrl1", posts.get(0).getPreviewUrl());
        assertEquals("Post 2", posts.get(1).getName());
        assertEquals("previewUrl2", posts.get(1).getPreviewUrl());
    }

    @Test
    void testGetSavedPosts_InvalidToken() {
        String token = "invalidToken";
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.getSavedPosts(token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(savedRepository, never()).findByUser(any());
    }

    @Test
    void testGetSavedPosts_UserNotFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.getSavedPosts(token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
        verify(savedRepository, never()).findByUser(any());
    }

    @Test
    void testCheckFavoritePost_Success() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post = new Post();
        post.setId(postId);
        Saved saved = new Saved();
        saved.setUser(user);
        saved.setPost(post);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(savedRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(saved));

        ResponseEntity<?> response = postController.checkFavoritePost(token, postId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    void testCheckFavoritePost_InvalidToken() {
        String token = "invalidToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.checkFavoritePost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testCheckFavoritePost_UserNotFound() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.checkFavoritePost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testCheckFavoritePost_PostNotFound() {
        String token = "validToken";
        Integer postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.checkFavoritePost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
    }

    @Test
    void testGetPosts_Success() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post1 = new Post();
        post1.setId(1);
        post1.setName("Post 1");
        post1.setS3Key("post1Key");
        post1.setUser(user);
        Post post2 = new Post();
        post2.setId(2);
        post2.setName("Post 2");
        post2.setS3Key("post2Key");
        post2.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findAll()).thenReturn(List.of(post1, post2));
        when(s3Service.generatePresignedPreviewUrl("post1Key")).thenReturn("previewUrl1");
        when(s3Service.generatePresignedPreviewUrl("post2Key")).thenReturn("previewUrl2");

        ResponseEntity<?> response = postController.getPosts(token, null, null, null, null);

        assertEquals(200, response.getStatusCodeValue());
        List<PostPreviewDTO> posts = (List<PostPreviewDTO>) response.getBody();
        assertEquals(2, posts.size());
        assertEquals("Post 1", posts.get(0).getName());
        assertEquals("previewUrl1", posts.get(0).getPreviewUrl());
        assertEquals("Post 2", posts.get(1).getName());
        assertEquals("previewUrl2", posts.get(1).getPreviewUrl());
    }

    @Test
    void testGetPosts_InvalidToken() {
        String token = "invalidToken";
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.getPosts(token, null, null, null, null);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testGetPosts_UserNotFound() {
        String token = "validToken";
        Integer userId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.getPosts(token, userId, null, null, null);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testGetPosts_NoPostsFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = postController.getPosts(token, null, null, null, null);

        assertEquals(200, response.getStatusCodeValue());
        List<PostPreviewDTO> posts = (List<PostPreviewDTO>) response.getBody();
        assertEquals(0, posts.size());
    }

    @Test
    void testGetPostsOfCurrentUser_Success() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        Post post1 = new Post();
        post1.setId(1);
        post1.setName("Post 1");
        post1.setS3Key("post1Key");
        post1.setUser(user);
        Post post2 = new Post();
        post2.setId(2);
        post2.setName("Post 2");
        post2.setS3Key("post2Key");
        post2.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.getPostsByUser(user)).thenReturn(List.of(post1, post2));
        when(s3Service.generatePresignedPreviewUrl("post1Key")).thenReturn("previewUrl1");
        when(s3Service.generatePresignedPreviewUrl("post2Key")).thenReturn("previewUrl2");

        ResponseEntity<?> response = postController.getPostsOfCurrentUser(token);

        assertEquals(200, response.getStatusCodeValue());
        List<PostPreviewDTO> posts = (List<PostPreviewDTO>) response.getBody();
        assertEquals(2, posts.size());
        assertEquals("Post 1", posts.get(0).getName());
        assertEquals("previewUrl1", posts.get(0).getPreviewUrl());
        assertEquals("Post 2", posts.get(1).getName());
        assertEquals("previewUrl2", posts.get(1).getPreviewUrl());
    }

    @Test
    void testGetPostsOfCurrentUser_InvalidToken() {
        String token = "invalidToken";
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = postController.getPostsOfCurrentUser(token);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testGetPostsOfCurrentUser_UserNotFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = postController.getPostsOfCurrentUser(token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }
}
