package com.backend.melodyHub.repository;

import com.backend.melodyHub.model.Comment;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> getCommentsByUser(User user);

    List<Comment> getCommentsByPost(Post post);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.user = (SELECT u FROM User u WHERE u.login = 'deleted_user') WHERE c.user = :originalUser")
    void reassignCommentsToDeletedUser(User originalUser);
}
