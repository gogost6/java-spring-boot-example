package com.example.demo.service;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AuthRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final AuthRepository authRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, AuthRepository authRepository,  CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.authRepository = authRepository;
        this.commentRepository = commentRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getAllPostsWithComments() {
        return postRepository.findAllWithComments();
    }

    public Post getById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    public Post createPost(String email, CreatePostRequest request) {
        User user = authRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Invalid email!"));
        Post post = new Post(user, request.title(), request.body());

        return postRepository.save(post);
    }

    public Post updatePost(Long id, String email, CreatePostRequest updated) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Post not found!"));
        User user = authRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!post.getOwner().equals(user)) {
            throw new ForbiddenException("User is not the owner of this post!");
        }

        post.setTitle(updated.title());
        post.setBody(updated.body());

        return postRepository.save(post);
    }

    public void deletePost(Long id, String email) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Post not found!"));
        User user = authRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        if (!post.getOwner().equals(user)) {
            throw new ForbiddenException("User is not the owner of this post!");
        }

        List<Comment> comment = commentRepository.findByPostId(id);
        commentRepository.deleteAll(comment);

        postRepository.deleteById(id);
    }
}
