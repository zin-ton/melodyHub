package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.AddPostDTO;
import com.backend.melodyHub.dto.DeletePostDTO;
import com.backend.melodyHub.dto.EditPostDTO;
import com.backend.melodyHub.dto.PostDTO;
import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.CategoryRepository;
import com.backend.melodyHub.repository.PostRepository;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@Tag(name = "Post Controller")
public class PostController {
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(PostController.class);
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    public PostController(PostRepository postRepository, JwtUtil jwtUtil, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @DeleteMapping("/deletePost")
    public ResponseEntity<?> deletePost(@RequestBody DeletePostDTO deletePostDTO) {
        TokenValidationResult result = jwtUtil.validateTokenFull(deletePostDTO.getToken());
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String token = deletePostDTO.getToken();
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isPresent()) {
                Optional<Post> post = postRepository.findById(deletePostDTO.getPostId());
                if (post.isPresent()) {
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

    @GetMapping("/postWithFilter")
    public ResponseEntity<?> getVideoWithFilter(@RequestHeader String token, @RequestParam String filter) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        if(filter == null){
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid filter"));
        }
        try{
            List<Post> posts = postRepository.findPostsByCategoryName(filter);
            List<PostDTO> returnPosts = new ArrayList<>();
            posts.forEach(post -> {
                List<String> categories = new ArrayList<>();
                post.getCategories().forEach(category -> categories.add(category.getName()));
                returnPosts.add(new PostDTO(post.getId(),
                        post.getSourceUrl(),
                        post.getDescription(),
                        post.getName() ,
                        post.getLeadsheet(),
                        post.getDateTime(),
                        categories,
                        post.getUser().getLogin()));
            });
            return ResponseEntity.ok(returnPosts);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to get the posts");
        }
    }

    @GetMapping("/getPosts")
    public ResponseEntity<?> getPosts() {
        try{
            List<Post> posts = postRepository.findAll();
            List<PostDTO> returnPosts = new ArrayList<>();
            posts.forEach(post -> {
                List<String> categories = new ArrayList<>();
                post.getCategories().forEach(category -> categories.add(category.getName()));
                returnPosts.add(new PostDTO(post.getId(),
                        post.getSourceUrl(),
                        post.getDescription(),
                        post.getName() ,
                        post.getLeadsheet(),
                        post.getDateTime(),
                        categories,
                        post.getUser().getLogin()));
            });
            return ResponseEntity.ok(returnPosts);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to get the posts");
        }
    }

    @PostMapping("/addPost")
    public ResponseEntity<?> addPost(@RequestBody AddPostDTO post) {
        TokenValidationResult result = jwtUtil.validateTokenFull(post.getToken());
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String token = post.getToken();
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isPresent()) {
                Post newPost = new Post();
                newPost.setSourceUrl(post.getPostDTO().getSourceUrl());
                newPost.setDescription(post.getPostDTO().getDescription());
                newPost.setName(post.getPostDTO().getName());
                newPost.setLeadsheet(post.getPostDTO().getLeadsheet());
                newPost.setDateTime(post.getPostDTO().getDateTime() != null
                        ? post.getPostDTO().getDateTime()
                        : LocalDateTime.now());

                Set<Category> categories = new HashSet<>();
                for (String categoryName : post.getPostDTO().getCategories()) {
                    Category category = categoryRepository.findByName(categoryName)
                            .orElseGet(() -> {
                                Category newCategory = new Category();
                                newCategory.setName(categoryName);
                                return newCategory;
                            });
                    if (category.getId() == null) {
                        categoryRepository.save(category);
                    }
                    categories.add(category);
                }
                newPost.setCategories(categories);
                newPost.setUser(user.get());
                postRepository.save(newPost);
                return ResponseEntity.ok("Post added successfully");
            } else {
                return ResponseEntity.badRequest().body("User not found");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to add the posts");
        }
    }

    @PostMapping("/editPost")
    public ResponseEntity<?> editPost(@RequestBody EditPostDTO post) {
        TokenValidationResult result = jwtUtil.validateTokenFull(post.getToken());
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String token = post.getToken();
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isPresent()) {
                Optional<Post> postFromDb = postRepository.findById(post.getPostDTO().getId());
                if (postFromDb.isPresent()) {
                    Post postToEdit = postFromDb.get();
                    postToEdit.setSourceUrl(post.getPostDTO().getSourceUrl());
                    postToEdit.setDescription(post.getPostDTO().getDescription());
                    postToEdit.setName(post.getPostDTO().getName());
                    postToEdit.setLeadsheet(post.getPostDTO().getLeadsheet());
                    postToEdit.setDateTime(post.getPostDTO().getDateTime() != null
                            ? post.getPostDTO().getDateTime()
                            : LocalDateTime.now());

                    Set<Category> categories = new HashSet<>();
                    for (String categoryName : post.getPostDTO().getCategories()) {
                        Category category = categoryRepository.findByName(categoryName)
                                .orElseGet(() -> {
                                    Category newCategory = new Category();
                                    newCategory.setName(categoryName);
                                    return newCategory;
                                });
                        if (category.getId() == null) {
                            categoryRepository.save(category);
                        }
                        categories.add(category);
                    }
                    postToEdit.setCategories(categories);
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


}
