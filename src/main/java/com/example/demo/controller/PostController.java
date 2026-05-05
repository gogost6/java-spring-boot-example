package com.example.demo.controller;

import java.util.List;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Post;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{post_id}/comments")
    public List<Comment> getCommentsByPostId(@PathVariable Long post_id) {
        return commentService.getCommentsByPostId(post_id);
    }

    @PostMapping
    public Post createPost(@Valid @RequestBody CreatePostRequest request) {
        Post post = new Post(request.title(), request.body());
        return postService.createPost(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post updated) {
        return postService.updatePost(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postService.deletePost(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}