package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.PostServiceExternal;

@RestController
public class PostController {

    private final PostServiceExternal postServiceExternal;
    private final PostRepository postRepository;

    public PostController(PostRepository postRepository, PostServiceExternal postServiceExternal) {
        this.postRepository = postRepository;
        this.postServiceExternal = postServiceExternal;
    }

    @GetMapping("/api/posts")
    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    @PostMapping("/api/posts")
    public Post createPost(@RequestBody Post post) {
        return postRepository.save(post);
    }

    @PutMapping("/api/posts/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post updated) {
        return postRepository.findById(id)
                .map(post -> {
                    post.setTitle(updated.getTitle());
                    post.setBody(updated.getBody());
                    return ResponseEntity.ok(postRepository.save(post));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/external-posts")
    public List<com.example.demo.model.Post> getExternalPosts() {
        return postServiceExternal.getPosts();
    }
}