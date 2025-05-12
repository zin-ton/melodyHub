package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.S3Service;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.dto.PostDTO;
import com.backend.melodyHub.dto.PostPreviewDTO;
import com.backend.melodyHub.model.Category;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.Saved;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.CategoryRepository;
import com.backend.melodyHub.repository.PostRepository;
import com.backend.melodyHub.repository.SavedRepository;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
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
    private final S3Service s3Service;
    private final SavedRepository savedRepository;

    public PostController(PostRepository postRepository, JwtUtil jwtUtil, UserRepository userRepository, CategoryRepository categoryRepository, S3Service s3Service, SavedRepository savedRepository) {
        this.postRepository = postRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.savedRepository = savedRepository;
        this.s3Service = s3Service;
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

    @GetMapping("/postsByCategory")
    public ResponseEntity<?> getPostByCategory(@RequestHeader String token, @RequestParam List<Integer> filter) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        if (filter.isEmpty()) {
            List<PostPreviewDTO> returnPosts = new ArrayList<>();
            List<Post> posts = postRepository.findAll();
            for (Post post : posts) {
                String previewUrl = s3Service.generatePresignedPreviewUrl(post.getS3Key());
                returnPosts.add(PostPreviewDTO.fromPost(post, previewUrl));
            }
            return ResponseEntity.ok(returnPosts);
        }
        try {
            Set<Category> categories = new HashSet<>();
            for (Integer categoryId : filter) {
                if (!categoryRepository.existsById(categoryId)) {
                    return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                }
                Optional<Category> category = categoryRepository.findById(categoryId);
                category.ifPresent(categories::add);
            }
            List<Post> posts = postRepository.findPostsByCategories(categories);
            List<PostPreviewDTO> returnPosts = new ArrayList<>();
            for (Post post : posts) {
                String previewUrl = s3Service.generatePresignedPreviewUrl(post.getS3Key());
                returnPosts.add(PostPreviewDTO.fromPost(post, previewUrl));
            }
            if (returnPosts.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(returnPosts);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("An error occurred while trying to get the posts");
        }
    }

    @GetMapping("/getPosts")
    public ResponseEntity<?> getPosts(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to access this resource");
        try {
            List<Post> posts = postRepository.findAll();
            List<PostPreviewDTO> returnPosts = new ArrayList<>();
            for (Post post : posts) {
                String previewUrl = s3Service.generatePresignedPreviewUrl(post.getS3Key());
                returnPosts.add(PostPreviewDTO.fromPost(post, previewUrl));
            }
            if (returnPosts.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(returnPosts);
        } catch (Exception e) {
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
                for (Integer categoryId : post.getCategories()) {
                    if (!categoryRepository.existsById(categoryId)) {
                        return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                    }
                    Optional<Category> category = categoryRepository.findById(categoryId);
                    category.ifPresent(categories::add);
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
                    if (!Objects.equals(postToEdit.getUser().getId(), user.get().getId())) {
                        return ResponseEntity.badRequest().body("You are not the owner of this post");
                    }
                    postToEdit.setS3Key(post.getS3Key());
                    postToEdit.setDescription(post.getDescription());
                    postToEdit.setName(post.getName());
                    postToEdit.setLeadsheet(post.getLeadsheet());
                    Set<Category> categories = new HashSet<>();
                    for (Integer categoryId : post.getCategories()) {
                        if (!categoryRepository.existsById(categoryId)) {
                            return ResponseEntity.badRequest().body("Category with id " + categoryId + " does not exist");
                        }
                        Optional<Category> category = categoryRepository.findById(categoryId);
                        category.ifPresent(categories::add);
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

    @GetMapping("getPost")
    public ResponseEntity<?> getPost(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if(!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        try{
            Optional<Post> post = postRepository.findById(postId);
            if (post.isPresent()) {
                return ResponseEntity.ok(PostDTO.fromPost(post.get()));
            }
            return ResponseEntity.badRequest().body("Post not found");
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @PostMapping("addToFavorites")
    public ResponseEntity<?> addToFavorites(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if(!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try{
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            Post post = opt_post.get();
            Optional<Saved> saved = savedRepository.findByUserAndPost(user, post);
            if (saved.isPresent()) return ResponseEntity.badRequest().body("Post already saved");
            else{
                Saved new_saved = new Saved();
                new_saved.setUser(user);
                new_saved.setPost(post);
                savedRepository.save(new_saved);
                return ResponseEntity.ok("Post saved successfully");
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }

    @DeleteMapping("deleteFromFavorites")
    public ResponseEntity<?> deleteFromFavorites(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if(!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try{
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            Optional<Post> opt_post = postRepository.findById(postId);
            if (opt_post.isEmpty()) return ResponseEntity.badRequest().body("Post not found");
            Post post = opt_post.get();
            Optional<Saved> saved = savedRepository.findByUserAndPost(user, post);
            if (saved.isEmpty()) return ResponseEntity.badRequest().body("Post not saved");
            else{
                savedRepository.delete(saved.get());
                return ResponseEntity.ok("Post removed from saved successfully");
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }
    @Transactional
    @GetMapping("getSavedPosts")
    public ResponseEntity<?> getSavedPosts(@RequestHeader String token) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if(!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        try{
            Optional<User> opt_user = userRepository.findByLogin(username);
            if (opt_user.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            User user = opt_user.get();
            List<Saved> savedPosts = savedRepository.findByUser(user);
            List<PostDTO> postsDTO = new ArrayList<>();
            for(Saved post: savedPosts){
                postsDTO.add(PostDTO.fromPost(post.getPost()));
            }
            return ResponseEntity.ok(postsDTO);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body("something went wrong");
        }
    }
}
