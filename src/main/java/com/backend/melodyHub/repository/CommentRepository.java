package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Comment;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> getCommentsByUser(User user);

    List<Comment> getCommentsByPost(Post post);
}
