package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Comment;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import jakarta.annotation.Nullable;

public class CommentDTO {
    private Integer id;
    private String content;
    private Integer postId;
    private Integer replyToId;

    public static CommentDTO toCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setPostId(comment.getPost().getId());
        if (comment.getReplyTo() != null) {
            commentDTO.setReplyToId(comment.getReplyTo().getId());
        }
        return commentDTO;
    }

    public Comment toComment(User user, Post post, @Nullable Comment replyTo) {
        Comment comment = new Comment();
        comment.setContent(this.content);
        comment.setUser(user);
        comment.setPost(post);
        if (replyTo != null) {
            comment.setReplyTo(replyTo);
        }
        return comment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Integer replyToId) {
        this.replyToId = replyToId;
    }
}
