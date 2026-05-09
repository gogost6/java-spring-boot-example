package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.AuthRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;

@SpringBootTest
class PostServiceCacheTest {

    @Autowired
    PostService postService;

    @Autowired
    CacheManager cacheManager;

    @MockitoBean
    PostRepository postRepository;

    @MockitoBean
    AuthRepository authRepository;

    @MockitoBean
    CommentRepository commentRepository;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("posts").clear();
    }

    @Test
    void getById_firstCallHitsRepository() {
        Post post = new Post("Title", "Body");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.getById(1L);

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    void getById_secondCallReturnsCachedResult() {
        Post post = new Post("Title", "Body");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.getById(1L);
        postService.getById(1L);

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    void getById_cacheIsPopulatedAfterFirstCall() {
        Post post = new Post("Title", "Body");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.getById(1L);

        Object cached = cacheManager.getCache("posts").get(1L).get();
        assertThat(cached).isSameAs(post);
    }

    @Test
    void updatePost_evictsCacheEntry() {
        Post post = new Post("Title", "Body");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(post);

        postService.getById(1L);

        assertThat(cacheManager.getCache("posts").get(1L)).isNotNull();
        System.out.println(cacheManager.getCache("posts").get(1L));

        User owner = new User("owner@example.com", "hash", Set.of(Role.USER));
        when(authRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        post.setOwner(owner);
        postService.updatePost(1L, "owner@example.com", new CreatePostRequest("New Title", "New Body"));

        assertThat(cacheManager.getCache("posts").get(1L)).isNull();
    }
}
