package com.backend.melodyHub.controller;

import com.backend.melodyHub.component.JwtUtil;
import com.backend.melodyHub.component.TokenValidationResult;
import com.backend.melodyHub.model.Like;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import com.backend.melodyHub.repository.LikeRepository;
import com.backend.melodyHub.repository.PostRepository;
import com.backend.melodyHub.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Tag(name = "Like Controller")
public class LikeController {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final Logger logger = LoggerFactory.getLogger(LikeController.class);

    public LikeController(JwtUtil jwtUtil, UserRepository userRepository, PostRepository postRepository, LikeRepository likeRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
    }

    @PostMapping("/likePost")
    public ResponseEntity<String> likePost(@RequestHeader String token, @RequestParam Integer postId) {
        TokenValidationResult result = jwtUtil.validateTokenFull(token);
        if (!result.isValid())
            return ResponseEntity.badRequest().body(result.getErrorMessage().orElse("Invalid token"));
        String username = jwtUtil.extractUsername(token);
        Optional<User> opt_user = userRepository.findByLogin(username);
        if (opt_user.isEmpty()) return ResponseEntity.notFound().build();
        User user = opt_user.get();
        Optional<Post> opt_post = postRepository.findById(postId);
        if (opt_post.isEmpty()) return ResponseEntity.notFound().build();
        Post post = opt_post.get();
        Optional<Like> like = likeRepository.findByUserAndPost(user, post);
        if (like.isPresent()) return ResponseEntity.badRequest().body("Like already exists");
        Like new_like = new Like();
        new_like.setUser(user);
        new_like.setPost(post);
        try {
            likeRepository.save(new_like);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Something went wrong");
        }
        return ResponseEntity.ok().build();
    }
}
