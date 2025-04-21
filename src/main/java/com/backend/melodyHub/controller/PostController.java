package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.TokenValidationResult;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                    if(!Objects.equals(post.get().getUser().getId(), user.get().getId())) {
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

    @GetMapping("/postsByCategory")
    public ResponseEntity<?> getPostByCategory(@RequestHeader String token, @RequestParam List<Integer> filter) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        if(filter == null){
            List<PostDTO> returnPosts = new ArrayList<>();
            List<Post> posts = postRepository.findAll();
            posts.forEach(post -> returnPosts.add(PostDTO.fromPost(post)));
            return ResponseEntity.ok(returnPosts);
        }
        try{
            Set<Category> categories = new HashSet<>();
            for(Integer categoryId : filter){
                if(!categoryRepository.existsById(categoryId)){
                    return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                }
                categories.add(categoryRepository.findById(categoryId).get());
            }
            List<Post> posts = postRepository.findPostsByCategories(categories);
            List<PostDTO> returnPosts = new ArrayList<>();
            posts.forEach(post -> returnPosts.add(PostDTO.fromPost(post)));
            if(returnPosts.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(returnPosts);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to get the posts");
        }
    }

    @GetMapping("/getPosts")
    public ResponseEntity<?> getPosts(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to access this resource");
        try{
            List<Post> posts = postRepository.findAll();
            List<PostDTO> returnPosts = new ArrayList<>();
            posts.forEach(post -> returnPosts.add(PostDTO.fromPost(post)));
            if(returnPosts.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(returnPosts);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to get the posts");
        }
    }

    @PostMapping("/addPost")
    public ResponseEntity<?> addPost(@RequestBody PostDTO post, @RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try {
            Optional<User> user = userRepository.findByLogin(username);
            if (user.isPresent()) {
                Post newPost;
                Set<Category> categories = new HashSet<>();
                for(Integer categoryId : post.getCategories()){
                    if(!categoryRepository.existsById(categoryId)){
                        return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                    }
                    categories.add(categoryRepository.findById(categoryId).get());
                }
                newPost = post.toPost(user.get(), categories);
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
                    if(!Objects.equals(postToEdit.getUser().getId(), user.get().getId())) {
                        return ResponseEntity.badRequest().body("You are not the owner of this post");
                    }
                    postToEdit.setSourceUrl(post.getSourceUrl());
                    postToEdit.setDescription(post.getDescription());
                    postToEdit.setName(post.getName());
                    postToEdit.setLeadsheet(post.getLeadsheet());
                    Set<Category> categories = new HashSet<>();
                    for(Integer categoryId : post.getCategories()){
                        if(!categoryRepository.existsById(categoryId)){
                            return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                        }
                        categories.add(categoryRepository.findById(categoryId).get());
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
