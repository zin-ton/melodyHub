package com.backend.melodyHub.tests;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.controller.CommentController;
import com.backend.melodyHub.dto.CommentDTO;
import com.backend.melodyHub.model.Comment;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.CommentRepository;
import com.backend.melodyHub.repository.PostRepository;
import com.backend.melodyHub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testAddComment_Success() {
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPostId(1);
        commentDTO.setContent("Test comment");
        TokenValidationResult validationResult = new TokenValidationResult(true, null); // Use null instead of Optional.empty()
        User user = new User();
        Post post = new Post();
        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());
        ResponseEntity<?> response = commentController.addComment(token, commentDTO);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Comment added successfully", response.getBody());
    }

    @Test
    void testAddComment_InvalidToken() {
        String invalidToken = "invalidToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPostId(1);
        commentDTO.setContent("Test comment");
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token"); // Use String directly
        when(jwtUtil.validateTokenFull(invalidToken)).thenReturn(validationResult);
        ResponseEntity<?> response = commentController.addComment(invalidToken, commentDTO);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testAddComment_PostNotFound() {
        // Arrange
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPostId(999); // Non-existent post ID
        commentDTO.setContent("Test comment");
        TokenValidationResult validationResult = new TokenValidationResult(true, null); // Use null instead of Optional.empty()
        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(new User()));
        when(postRepository.findById(999)).thenReturn(Optional.empty()); // Post not found

        // Act
        ResponseEntity<?> response = commentController.addComment(token, commentDTO);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
    }


    @Test
    void testAddComment_ReplyToCommentNotFound() {
        // Arrange
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPostId(1);
        commentDTO.setContent("Test reply comment");
        commentDTO.setReplyToId(999); // Non-existent comment ID
        TokenValidationResult validationResult = new TokenValidationResult(true, null); // Use null instead of Optional.empty()
        User user = new User();
        Post post = new Post();
        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(commentRepository.findById(999)).thenReturn(Optional.empty()); // Reply-to comment not found

        // Act
        ResponseEntity<?> response = commentController.addComment(token, commentDTO);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment to reply to not found", response.getBody());
    }

    @Test
    void testAddComment_EmptyContent() {
        // Arrange
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPostId(1);
        commentDTO.setContent(""); // Empty content
        TokenValidationResult validationResult = new TokenValidationResult(true, null); // Use null instead of Optional.empty()
        User user = new User();
        Post post = new Post();
        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // Act
        ResponseEntity<?> response = commentController.addComment(token, commentDTO);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment content cannot be empty", response.getBody());
    }

    @Test
    void testAddComment_InvalidReplyToId() {
        // Arrange
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPostId(1);
        commentDTO.setContent("Test reply comment");
        commentDTO.setReplyToId(-1); // Invalid replyToId
        TokenValidationResult validationResult = new TokenValidationResult(true, null); // Use null instead of Optional.empty()
        User user = new User();
        Post post = new Post();
        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // Act
        ResponseEntity<?> response = commentController.addComment(token, commentDTO);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment to reply to not found", response.getBody());
    }


    @Test
    void testDeleteComment_Success() {
        String token = "validToken";
        Integer commentId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        ResponseEntity<?> response = commentController.deleteComment(token, commentId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Comment deleted successfully", response.getBody());
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_InvalidToken() {
        String token = "invalidToken";
        Integer commentId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = commentController.deleteComment(token, commentId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        String token = "validToken";
        Integer commentId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = commentController.deleteComment(token, commentId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment not found", response.getBody());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void testDeleteComment_UnauthorizedAccess() {
        String token = "validToken";
        Integer commentId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        User anotherUser = new User();
        anotherUser.setId(2);
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(anotherUser);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        ResponseEntity<?> response = commentController.deleteComment(token, commentId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("You are not the owner of this comment", response.getBody());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void testEditComment_Success() {
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1);
        commentDTO.setContent("Updated content");
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        Comment comment = new Comment();
        comment.setId(1);
        comment.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        ResponseEntity<?> response = commentController.editComment(token, commentDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Comment edited successfully", response.getBody());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testEditComment_InvalidToken() {
        String token = "invalidToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1);
        commentDTO.setContent("Updated content");
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = commentController.editComment(token, commentDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testEditComment_CommentNotFound() {
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(999);
        commentDTO.setContent("Updated content");
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = commentController.editComment(token, commentDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment not found", response.getBody());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testEditComment_UnauthorizedAccess() {
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1);
        commentDTO.setContent("Updated content");
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        User anotherUser = new User();
        anotherUser.setId(2);
        Comment comment = new Comment();
        comment.setId(1);
        comment.setUser(anotherUser);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        ResponseEntity<?> response = commentController.editComment(token, commentDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("You are not the owner of this comment", response.getBody());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testEditComment_EmptyContent() {
        String token = "validToken";
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(1);
        commentDTO.setContent(""); // Empty content
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        Comment comment = new Comment();
        comment.setId(1);
        comment.setUser(user);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        ResponseEntity<?> response = commentController.editComment(token, commentDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment content cannot be empty", response.getBody());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testGetCommentCreatedByUser_Success() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        user.setS3Key("testKey");
        Post post = new Post();
        post.setId(1); // Initialize the Post object
        Comment comment = new Comment();
        comment.setId(1);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setPost(post); // Set the Post object

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.getCommentsByUser(user)).thenReturn(List.of(comment));
        when(s3Service.generatePresignedImageUrl("testKey")).thenReturn("testUrl");

        ResponseEntity<?> response = commentController.getCommentCreatedByUser(token);

        assertEquals(200, response.getStatusCodeValue());
        List<CommentDTO> comments = (List<CommentDTO>) response.getBody();
        assertEquals(1, comments.size());
        assertEquals("Test comment", comments.get(0).getContent());
    }

    @Test
    void testGetCommentCreatedByUser_InvalidToken() {
        String token = "invalidToken";
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = commentController.getCommentCreatedByUser(token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testGetCommentCreatedByUser_UserNotFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = commentController.getCommentCreatedByUser(token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testGetCommentCreatedByUser_NoCommentsFound() {
        String token = "validToken";
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(jwtUtil.extractUsername(token)).thenReturn("testUser");
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(user));
        when(commentRepository.getCommentsByUser(user)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = commentController.getCommentCreatedByUser(token);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment not found", response.getBody());
    }

    @Test
    void testGetCommentByPost_Success() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        User user = new User();
        user.setId(1);
        user.setLogin("testUser");
        user.setS3Key("testKey");
        Post post = new Post();
        post.setId(postId);
        Comment comment = new Comment();
        comment.setId(1);
        comment.setContent("Test comment");
        comment.setUser(user);
        comment.setPost(post);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.getCommentsByPost(post)).thenReturn(List.of(comment));
        when(s3Service.generatePresignedImageUrl("testKey")).thenReturn("testUrl");

        ResponseEntity<?> response = commentController.getCommentByPost(token, postId);

        assertEquals(200, response.getStatusCodeValue());
        List<CommentDTO> comments = (List<CommentDTO>) response.getBody();
        assertEquals(1, comments.size());
        assertEquals("Test comment", comments.get(0).getContent());
    }

    @Test
    void testGetCommentByPost_InvalidToken() {
        String token = "invalidToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = commentController.getCommentByPost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testGetCommentByPost_PostNotFound() {
        String token = "validToken";
        Integer postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = commentController.getCommentByPost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
    }

    @Test
    void testGetCommentByPost_NoCommentsFound() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        Post post = new Post();
        post.setId(postId);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.getCommentsByPost(post)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = commentController.getCommentByPost(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Comment not found", response.getBody());
    }

    @Test
    void testGetSortedComments_InvalidToken() {
        String token = "invalidToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(false, "Invalid token");

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);

        ResponseEntity<?> response = commentController.getSortedComments(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void testGetSortedComments_PostNotFound() {
        String token = "validToken";
        Integer postId = 999;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = commentController.getSortedComments(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Post not found", response.getBody());
    }

    @Test
    void testGetSortedComments_UserNotFound() {
        String token = "validToken";
        Integer postId = 1;
        TokenValidationResult validationResult = new TokenValidationResult(true, null);
        Post post = new Post();
        post.setId(postId);

        when(jwtUtil.validateTokenFull(token)).thenReturn(validationResult);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = commentController.getSortedComments(token, postId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

}
