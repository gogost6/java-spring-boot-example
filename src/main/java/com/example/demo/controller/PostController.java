package com.example.demo.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CommentResponse;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;

import jakarta.validation.Valid;

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
    public Page<PostResponse> getAllPosts(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> page = (search != null && !search.isBlank())
                ? postService.searchPosts(search, pageable)
                : postService.getAllPosts(pageable);
        return page.map(p -> new PostResponse(
                p.getId(),
                p.getTitle(),
                p.getBody(),
                p.getOwner().getEmail()));
    }

    @GetMapping("/{post_id}")
    public PostResponse getPostById(@PathVariable Long post_id) {
        Post post = postService.getById(post_id);
        return new PostResponse(post.getId(), post.getTitle(), post.getBody(), post.getOwner().getEmail());
    }

    @GetMapping("/{post_id}/comments")
    public Page<CommentResponse> getCommentsByPostId(
            @PathVariable Long post_id,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return commentService.getCommentsByPostId(post_id, pageable);
    }

    @PostMapping
    public PostResponse createPost(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreatePostRequest request) {
        Post post = postService.createPost(jwt.getSubject(), request);
        return new PostResponse(post.getId(), post.getTitle(), post.getBody(), post.getOwner().getEmail());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePostRequest updated) {
        postService.updatePost(id, jwt.getSubject(), updated);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        postService.deletePost(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}