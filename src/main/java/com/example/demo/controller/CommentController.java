package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CommentResponse;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{post_id}/post")
    public List<CommentResponse> getAllByPostId(@PathVariable Long post_id) {
        return this.commentService.getCommentsByPostId(post_id);
    }

    @GetMapping("/{comment_id}")
    public CommentResponse getCommentById(@PathVariable Long comment_id) {
        return this.commentService.getCommentById(comment_id);
    }

    @PostMapping("/{post_id}")
    public CommentResponse addComment(
            @PathVariable Long post_id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommentRequest comment) {
        return commentService.create(post_id, jwt.getSubject(), comment.content());
    }

    @PutMapping("/{comment_id}")
    public CommentResponse updateComment(
            @PathVariable Long comment_id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommentRequest comment) {
        return commentService.update(comment_id, jwt.getSubject(), comment.content());
    }

    @DeleteMapping("/{comment_id}")
    public void deleteById(@PathVariable Long comment_id, @AuthenticationPrincipal Jwt jwt) {
        commentService.delete(comment_id, jwt.getSubject());
    }
}
