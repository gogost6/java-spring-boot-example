package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{postId}")
    public List<Comment> getByPostId(@PathVariable Long postId) {
        return this.commentService.getCommentsByPostId(postId);
    }

    @PostMapping("/{post_id}")
    public Comment addComment(@PathVariable Post post_id, @RequestBody Comment comment) {
        return commentService.create(post_id, comment.getContent());
    }

    @PutMapping("/{comment_id}")
    public Comment updateComment(@PathVariable Long comment_id, @RequestBody Comment comment) {
        return commentService.update(comment_id, comment.getContent());
    }

    @DeleteMapping("/{comment_id}")
    public void deleteById(@PathVariable Long comment_id) {
        commentService.delete(comment_id);
    }
}
