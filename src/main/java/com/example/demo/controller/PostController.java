package com.example.demo.controller;

import java.util.List;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public List<PostResponse> getAllPosts() {
        return postService.getAllPosts()
                .stream()
                .map(p -> new PostResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getBody(),
                        p.getOwner().getEmail()))
                .toList();
    }

    @GetMapping("/{post_id}")
    public PostResponse getPostById(@PathVariable Long post_id) {
        Post post = postService.getById(post_id);
        return new PostResponse(post.getId(), post.getTitle(), post.getBody(), post.getOwner().getEmail());
    }

    @GetMapping("/{post_id}/comments")
    public List<Comment> getCommentsByPostId(@PathVariable Long post_id) {
        return commentService.getCommentsByPostId(post_id);
    }

    @PostMapping
    public PostResponse createPost(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreatePostRequest request) {
        Post post = postService.createPost(jwt.getSubject(), request);
        return new PostResponse(post.getId(), post.getTitle(), post.getBody(), post.getOwner().getEmail());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreatePostRequest updated) {
        postService.updatePost(id, jwt.getSubject(), updated);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        postService.deletePost(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}