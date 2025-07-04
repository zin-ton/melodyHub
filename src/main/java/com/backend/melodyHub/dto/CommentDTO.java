package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.Comment;
import com.backend.melodyHub.model.Post;
import com.backend.melodyHub.model.User;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDTO {
    @Nullable
    private Integer id;
    private String content;
    private Integer postId;
    @Nullable
    private Integer replyToId;
    private Integer userId;
    private String userName;
    @Nullable
    private LocalDateTime dateTime;
    private String authorProfilePictureUrl;
    @Nullable
    private List<CommentDTO> replies;

    public static CommentDTO toCommentDTO(Comment comment, String userName, @Nullable String authorProfilePictureUrl) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setPostId(comment.getPost().getId());
        if (comment.getReplyTo() != null) {
            commentDTO.setReplyToId(comment.getReplyTo().getId());
        }
        commentDTO.setUserId(comment.getUser().getId());
        commentDTO.setUserName(userName);
        commentDTO.setDateTime(comment.getDateTime());
        commentDTO.setAuthorProfilePictureUrl(authorProfilePictureUrl);
        return commentDTO;
    }

    public Comment toComment(User user, Post post, @Nullable Comment replyTo) {
        Comment comment = new Comment();
        comment.setContent(this.content);
        comment.setUser(user);
        comment.setPost(post);
        comment.setDateTime(LocalDateTime.now());
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getAuthorProfilePictureUrl() {
        return authorProfilePictureUrl;
    }

    public void setAuthorProfilePictureUrl(String authorProfilePictureUrl) {
        this.authorProfilePictureUrl = authorProfilePictureUrl;
    }

    public List<CommentDTO> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentDTO> replies) {
        this.replies = replies;
    }
}
