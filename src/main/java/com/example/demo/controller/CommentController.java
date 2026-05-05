package com.example.demo.controller;

import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{post_id}/post")
    public List<Comment> getAllByPostId(@PathVariable Long post_id) {
        return this.commentService.getCommentsByPostId(post_id);
    }

    @GetMapping("/{comment_id}")
    public Comment getCommentById(@PathVariable Long comment_id) {
        return this.commentService.getCommentById(comment_id);
    }

    @PostMapping("/{post_id}")
    public Comment addComment(@PathVariable Long post_id, @Valid @RequestBody CreateCommentRequest comment) {
        return commentService.create(post_id, comment.content());
    }

    @PutMapping("/{comment_id}")
    public Comment updateComment(@PathVariable Long comment_id, @Valid @RequestBody CreateCommentRequest comment) {
        return commentService.update(comment_id, comment.content());
    }

    @DeleteMapping("/{comment_id}")
    public void deleteById(@PathVariable Long comment_id) {
        commentService.delete(comment_id);
    }
}
