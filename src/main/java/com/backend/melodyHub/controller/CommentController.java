package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.CommentDTO;
import com.backend.melodyHub.model.Comment;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.CommentRepository;
import com.backend.melodyHub.repository.PostRepository;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@Tag(name = "Comment Controller")
public class CommentController {
    private final CommentRepository commentRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;

    public CommentController(CommentRepository commentRepository, JwtUtil jwtUtil, UserRepository userRepository, PostRepository postRepository, S3Service s3Service) {
        this.commentRepository = commentRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.s3Service = s3Service;
    }

    @PostMapping("addComment")
    public ResponseEntity<?> addComment(@RequestHeader String token, @RequestBody CommentDTO comment) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            Optional<Post> opt_post = postRepository.findById(comment.getPostId());
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            if (comment.getReplyToId() != null) {
                Optional<Comment> opt_replyTo = commentRepository.findById(comment.getReplyToId());
                if (opt_replyTo.isEmpty()) return ResponseEntity.badRequest().body("Comment to reply to not found");
                else {
                    Comment newComment = comment.toComment(opt_user.get(), opt_post.get(), opt_replyTo.get());
                    commentRepository.save(newComment);
                    return ResponseEntity.ok().body("Comment added successfully");
                }
            } else {
                Comment newComment = comment.toComment(opt_user.get(), opt_post.get(), null);
                commentRepository.save(newComment);
                return ResponseEntity.ok().body("Comment added successfully");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @DeleteMapping("deleteComment")
    public ResponseEntity<?> deleteComment(@RequestHeader String token, @RequestParam Integer commentId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            Optional<Comment> opt_comment = commentRepository.findById(commentId);
            if (opt_comment.isEmpty()) return ResponseEntity.badRequest().body("Comment not found");
            if (!opt_comment.get().getUser().getId().equals(opt_user.get().getId()))
                return ResponseEntity.badRequest().body("You are not the owner of this comment");
            commentRepository.delete(opt_comment.get());
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @PutMapping("editComment")
    public ResponseEntity<?> editComment(@RequestHeader String token, @RequestBody CommentDTO comment) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            Optional<Comment> opt_comment = commentRepository.findById(comment.getId());
            if (opt_comment.isEmpty()) return ResponseEntity.badRequest().body("Comment not found");
            if (!opt_comment.get().getUser().getId().equals(opt_user.get().getId()))
                return ResponseEntity.badRequest().body("You are not the owner of this comment");
            Comment commentToEdit = opt_comment.get();
            commentToEdit.setContent(comment.getContent());
            commentRepository.save(commentToEdit);
            return ResponseEntity.ok("Comment edited successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @Transactional
    @GetMapping("getCommentCreatedByUser")
    public ResponseEntity<?> getCommentCreatedByUser(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            List<Comment> opt_comment = commentRepository.getCommentsByUser(opt_user.get());
            List<CommentDTO> comments = new ArrayList<>();
            if (opt_comment.isEmpty()) return ResponseEntity.badRequest().body("Comment not found");
            else {
                for (Comment comment : opt_comment) {

                    comments.add(CommentDTO.toCommentDTO(comment, comment.getUser().getLogin(), s3Service.generatePresignedImageUrl(comment.getUser().getS3Key())));
                }
            }
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @Transactional
    @GetMapping("getCommentByPost")
    public ResponseEntity<?> getCommentByPost(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            List<Comment> opt_comment = commentRepository.getCommentsByPost(opt_post.get());
            List<CommentDTO> comments = new ArrayList<>();
            if (opt_comment.isEmpty()) return ResponseEntity.badRequest().body("Comment not found");
            else {
                for (Comment comment : opt_comment) {
                    comments.add(CommentDTO.toCommentDTO(comment, comment.getUser().getLogin(), s3Service.generatePresignedImageUrl(comment.getUser().getS3Key())));
                }
            }
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @Transactional
    @GetMapping("getSortedComments")
    public ResponseEntity<?> getSortedComments(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));

        try {
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");

            Optional<User> opt_user = userRepository.findByLogin(jwtUtil.extractUsername(token));
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");

            Post post = opt_post.get();
            User currentUser = opt_user.get();
            User postOwner = post.getUser();

            List<Comment> allComments = commentRepository.getCommentsByPost(post);

            // Group comments by replyToId
            Map<Integer, List<Comment>> commentsByParentId = allComments.stream()
                    .collect(Collectors.groupingBy(
                            comment -> comment.getReplyTo() == null ? -1 : comment.getReplyTo().getId(),
                            HashMap::new,
                            Collectors.toList()
                    ));

            // Build hierarchical structure for all comments
            List<CommentDTO> sortedComments = buildCommentHierarchy(commentsByParentId, -1, allComments);

            return ResponseEntity.ok(sortedComments);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    private List<CommentDTO> buildCommentHierarchy(Map<Integer, List<Comment>> commentsByParentId, Integer parentId, List<Comment> group) {
        Integer effectiveParentId = (parentId == null) ? -1 : parentId;

        // Get comments for the current parentId
        List<Comment> comments = commentsByParentId.getOrDefault(effectiveParentId, Collections.emptyList());

        // Avoid duplicate processing
        Set<Integer> processedIds = new HashSet<>();

        return comments.stream()
                .filter(comment -> processedIds.add(comment.getId())) // Process each comment only once
                .map(comment -> {
                    CommentDTO dto = CommentDTO.toCommentDTO(
                            comment,
                            comment.getUser().getLogin(),
                            s3Service.generatePresignedImageUrl(comment.getUser().getS3Key())
                    );
                    // Recursively build replies
                    dto.setReplies(buildCommentHierarchy(commentsByParentId, comment.getId(), group));
                    return dto;
                })
                .toList();
    }

    private LocalDateTime getLastReplyDate(Comment comment) {
        LocalDateTime latest = comment.getDateTime();
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                LocalDateTime replyDate = getLastReplyDate(reply);
                if (replyDate.isAfter(latest)) {
                    latest = replyDate;
                }
            }
        }
        return latest;
    }


}
