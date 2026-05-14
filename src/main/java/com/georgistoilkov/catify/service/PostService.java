package com.georgistoilkov.catify.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.georgistoilkov.catify.dto.CreatePostRequest;
import com.georgistoilkov.catify.entity.Comment;
import com.georgistoilkov.catify.entity.Post;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.exception.ForbiddenException;
import com.georgistoilkov.catify.exception.ResourceNotFoundException;
import com.georgistoilkov.catify.repository.AuthRepository;
import com.georgistoilkov.catify.repository.CommentRepository;
import com.georgistoilkov.catify.repository.PostRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final AuthRepository authRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, AuthRepository authRepository,
            CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.authRepository = authRepository;
        this.commentRepository = commentRepository;
    }

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchPosts(String search, Pageable pageable) {
        return postRepository.findBySearch(search, pageable);
    }

    public List<Post> getAllPostsWithComments() {
        return postRepository.findAllWithComments();
    }

    @Cacheable(value = "posts", key = "#id")
    public Post getById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    public Post createPost(String email, CreatePostRequest request) {
        User user = authRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Invalid email!"));
        Post post = new Post(user, request.title(), request.body());

        return postRepository.save(post);
    }

    @CacheEvict(value = "posts", key = "#id")
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

    @CacheEvict(value = "posts", key = "#id")
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
