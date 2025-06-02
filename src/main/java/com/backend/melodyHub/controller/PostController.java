package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.PostDTO;
import com.backend.melodyHub.dto.PostPageDTO;
import com.backend.melodyHub.dto.PostPreviewDTO;
import com.backend.melodyHub.model.*;
import com.backend.melodyHub.repository.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Post Controller")
public class PostController {
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(PostController.class);
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final SavedRepository savedRepository;
    private final PostToCategoryRepository postToCategoryRepository;

    public PostController(PostRepository postRepository, JwtUtil jwtUtil, UserRepository userRepository, CategoryRepository categoryRepository, S3Service s3Service, SavedRepository savedRepository, PostToCategoryRepository postToCategoryRepository) {
        this.postRepository = postRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.savedRepository = savedRepository;
        this.s3Service = s3Service;
        this.postToCategoryRepository = postToCategoryRepository;
    }

    @DeleteMapping("/deletePost")
    public ResponseEntity<?> deletePost(@RequestHeader String token, @RequestBody int postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isPresent()) {
                Optional<Post> post = postRepository.findById(postId);
                if (post.isPresent()) {
                    if (!Objects.equals(post.get().getUser().getId(), user.get().getId())) {
                        return ResponseEntity.badRequest().body("You are not the owner of this post");
                    }
                    postRepository.delete(post.get());
                    return ResponseEntity.ok("Post deleted successfully");
                } else {
                    return ResponseEntity.badRequest().body("Post not found");
                }
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to delete the posts");
        }
    }


    @GetMapping("/getPosts")
    public ResponseEntity<?> getPosts(
            @RequestHeader String token,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false) String sort) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
        }

        try {
            List<Post> posts = postRepository.findAll();

            // Filter by userId
            if (userId != null) {
                Optional<User> user = userRepository.findById(userId);
                if (user.isEmpty()) {
                    return ResponseEntity.badRequest().body("User not found");
                }
                posts = posts.stream()
                        .filter(post -> post.getUser().getId().equals(userId))
                        .toList();
            }

            // Filter by categories
            if (categoryIds != null && !categoryIds.isEmpty()) {
                posts = posts.stream()
                        .filter(post -> {
                            Set<Integer> postCategoryIds = post.getPostToCategories().stream()
                                    .map(postToCategory -> postToCategory.getCategory().getId())
                                    .collect(Collectors.toSet());
                            return postCategoryIds.containsAll(categoryIds);
                        })
                        .toList();
            }

            // Sort posts
            if ("date".equalsIgnoreCase(sort)) {
                posts.sort((p1, p2) -> p2.getDateTime().compareTo(p1.getDateTime()));
            } else if ("likes".equalsIgnoreCase(sort)) {
                LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
                posts.sort((p1, p2) -> {
                    long likes1 = p1.getLikes().stream()
                            .filter(like -> like.getLikeDate() != null && like.getLikeDate().isAfter(oneMonthAgo))
                            .count();
                    long likes2 = p2.getLikes().stream()
                            .filter(like -> like.getLikeDate() != null && like.getLikeDate().isAfter(oneMonthAgo))
                            .count();
                    return Long.compare(likes2, likes1);
                });
            }

            // Convert to DTOs
            List<PostPreviewDTO> resultPosts = posts.stream()
                    .map(post -> PostPreviewDTO.fromPost(post, s3Service.generatePresignedPreviewUrl(post.getS3Key())))
                    .toList();

            return ResponseEntity.ok(resultPosts);
        } catch (Exception e) {
            logger.error("Error while fetching posts: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Something went wrong while fetching posts");
        }
    }

    @Transactional
    @PostMapping("/addPost")
    public ResponseEntity<?> addPost(@RequestBody PostDTO post, @RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid()) {
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        }

        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Validate categories
            Set<PostToCategory> postToCategories = new HashSet<>();
            for (Integer categoryId : post.getCategories()) {
                Optional<Category> category = categoryRepository.findById(categoryId);
                if (category.isEmpty()) {
                    return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                }
                PostToCategory postToCategory = new PostToCategory();
                postToCategory.setCategory(category.get());
                postToCategories.add(postToCategory);
            }

            // Save the post
            Post newPost = post.toPost(user.get());
            newPost.setDateTime(LocalDateTime.now());
            newPost = postRepository.save(newPost);

            // Associate categories
            for (PostToCategory postToCategory : postToCategories) {
                postToCategory.setPost(newPost);
            }
            postToCategoryRepository.saveAll(postToCategories);

            return ResponseEntity.ok("Post added successfully");
        } catch (Exception e) {
            logger.error("Error occurred while adding post: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An error occurred while trying to add the post");
        }
    }

    @PostMapping("/editPost")
    public ResponseEntity<?> editPost(@RequestBody PostDTO post, @RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isPresent()) {
                Optional<Post> postFromDb = postRepository.findById(post.getId());
                if (postFromDb.isPresent()) {
                    Post postToEdit = postFromDb.get();
                    if (!Objects.equals(postToEdit.getUser().getId(), user.get().getId())) {
                        return ResponseEntity.badRequest().body("You are not the owner of this post");
                    }
                    postToEdit.setS3Key(post.getS3Key());
                    postToEdit.setDescription(post.getDescription());
                    postToEdit.setName(post.getName());
                    postToEdit.setLeadsheetKey(post.getLeadsheetKey());

                    // Update categories
                    postToCategoryRepository.deleteAll(postToEdit.getPostToCategories());
                    Set<PostToCategory> postToCategories = new HashSet<>();
                    for (Integer categoryId : post.getCategories()) {
                        Optional<Category> category = categoryRepository.findById(categoryId);
                        if (category.isEmpty()) {
                            return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                        }
                        PostToCategory postToCategory = new PostToCategory();
                        postToCategory.setPost(postToEdit);
                        postToCategory.setCategory(category.get());
                        postToCategories.add(postToCategory);
                    }
                    postToCategoryRepository.saveAll(postToCategories);

                    postRepository.save(postToEdit);
                    return ResponseEntity.ok("Post edited successfully");
                } else {
                    return ResponseEntity.badRequest().body("Post not found");
                }
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to edit the posts");
        }
    }

    @GetMapping("/getPost")
    public ResponseEntity<?> getPost(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        try {
            Optional<Post> post = postRepository.findById(postId);
            if (post.isPresent()) {
                String leadsheetUrl = "";
                if (post.get().getLeadsheetKey() != null) {
                    leadsheetUrl = s3Service.generatePresignedLeadsheetUrl(post.get().getLeadsheetKey());
                }
                PostPageDTO returnPost = PostPageDTO.fromPost(post.get(), s3Service.generatePresignedPreviewUrl(post.get().getS3Key()), s3Service.generatePresignedVideoUrl(post.get().getS3Key()), leadsheetUrl);
                return ResponseEntity.ok(returnPost);
            }
            return ResponseEntity.badRequest().body("Post not found");
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @PostMapping("/addPostToFavorites")
    public ResponseEntity<?> addPostToFavorites(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            Post post = opt_post.get();
            Optional<Saved> saved = savedRepository.findByUserAndPost(user, post);
            if (saved.isPresent()) return ResponseEntity.badRequest().body("Post already saved");
            else {
                Saved new_saved = new Saved();
                new_saved.setUser(user);
                new_saved.setPost(post);
                savedRepository.save(new_saved);
                return ResponseEntity.ok("Post saved successfully");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @DeleteMapping("/deletePostFromFavorites")
    public ResponseEntity<?> deletePostFromFavorites(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            Post post = opt_post.get();
            Optional<Saved> saved = savedRepository.findByUserAndPost(user, post);
            if (saved.isEmpty()) return ResponseEntity.badRequest().body("Post not saved");
            else {
                savedRepository.delete(saved.get());
                return ResponseEntity.ok("Post removed from saved successfully");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @Transactional
    @GetMapping("/getSavedPosts")
    public ResponseEntity<?> getSavedPosts(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            List<Saved> savedPosts = savedRepository.findByUser(user);
            List<PostPreviewDTO> postsDTO = new ArrayList<>();
            for (Saved post : savedPosts) {
                postsDTO.add(PostPreviewDTO.fromPost(post.getPost(), s3Service.generatePresignedPreviewUrl(post.getPost().getS3Key())));
            }
            return ResponseEntity.ok(postsDTO);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @Transactional
    @GetMapping("checkFavoritePost")
    public ResponseEntity<?> checkFavoritePost(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            Post post = opt_post.get();
            Optional<Saved> saved = savedRepository.findByUserAndPost(user, post);
            if (saved.isPresent()) return ResponseEntity.ok(Boolean.TRUE);
            else return ResponseEntity.ok(Boolean.FALSE);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @GetMapping("getPostsOfCurrentUser")
    public ResponseEntity<?> getPostsOfCurrentUser(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
        }

        try {
            Optional<User> opt_user = userRepository.findByLogin(jwtUtil.extractUsername(token));
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("Invalid token");
            else {
                List<Post> posts = postRepository.getPostsByUser(opt_user.get());
                List<PostPreviewDTO> resultPosts = new ArrayList<>();
                for (Post post : posts) {
                    String previewUrl = s3Service.generatePresignedPreviewUrl(post.getS3Key());
                    resultPosts.add(PostPreviewDTO.fromPost(post, previewUrl));
                }
                return ResponseEntity.ok(resultPosts);
            }
        } catch (Exception e) {
            logger.error("Error while getting posts: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Something went wrong while getting posts");
        }
    }

}
