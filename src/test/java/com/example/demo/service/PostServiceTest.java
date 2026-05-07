package com.example.demo.service;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.AuthRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    AuthRepository authRepository;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    PostService postService;

    @Test
    void getAllPosts_shouldReturnPosts() {
        Post post1 = new Post("Title 1", "Body 1");
        Post post2 = new Post("Title 2", "Body 2");

        when(postRepository.findAll()).thenReturn(List.of(post1, post2));

        List<Post> result = postService.getAllPosts();

        assertEquals(2, result.size());
        verify(postRepository).findAll();
    }

    @Test
    void createPost_shouldSavePostWithOwner() {
        User owner = new User("owner@mail.com", "hashed-password");
        CreatePostRequest request = new CreatePostRequest("Title", "Body");

        when(authRepository.findByEmail("owner@mail.com"))
                .thenReturn(Optional.of(owner));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.createPost("owner@mail.com", request);

        assertEquals("Title", result.getTitle());
        assertEquals("Body", result.getBody());
        assertEquals(owner, result.getOwner());

        verify(authRepository).findByEmail("owner@mail.com");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_shouldThrow_whenUserDoesNotExist() {
        CreatePostRequest request = new CreatePostRequest("Title", "Body");

        when(authRepository.findByEmail("missing@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                postService.createPost("missing@mail.com", request)
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_shouldUpdate_whenUserIsOwner() {
        User owner = new User("owner@mail.com", "hashed-password");

        Post existingPost = new Post("Old title", "Old body");
        existingPost.setId(1L);
        existingPost.setOwner(owner);

        CreatePostRequest request = new CreatePostRequest("New title", "New body");

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(existingPost));

        when(postRepository.save(existingPost))
                .thenReturn(existingPost);

        when(authRepository.findByEmail("owner@mail.com"))
                .thenReturn(Optional.of(owner));

        Post result = postService.updatePost(1L, "owner@mail.com", request);

        assertEquals("New title", result.getTitle());
        assertEquals("New body", result.getBody());

        verify(postRepository).findById(1L);
        verify(postRepository).save(existingPost);
    }

    @Test
    void updatePost_shouldThrow_whenUserIsNotOwner() {
        User owner = new User("owner@mail.com", "hashed-password");

        Post existingPost = new Post("Old title", "Old body");
        existingPost.setId(1L);
        existingPost.setOwner(owner);

        CreatePostRequest request = new CreatePostRequest("New title", "New body");

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(existingPost));

        assertThrows(IllegalStateException.class, () ->
                postService.updatePost(1L, "other@mail.com", request)
        );

        verify(postRepository).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrow_whenPostDoesNotExist() {
        CreatePostRequest request = new CreatePostRequest("New title", "New body");

        when(postRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                postService.updatePost(999L, "owner@mail.com", request)
        );

        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_shouldDelete_whenUserIsOwner() {
        User owner = new User("owner@mail.com", "hashed-password");
        Post existingPost = new Post("Title", "Body");
        existingPost.setId(1L);
        existingPost.setOwner(owner);

        when(authRepository.findByEmail("owner@mail.com"))
                .thenReturn(Optional.of(owner));

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(existingPost));

        postService.deletePost(1L, "owner@mail.com");

        verify(postRepository).findById(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    void deletePost_shouldThrow_whenUserIsNotOwner() {
        User owner = new User("owner@mail.com", "hashed-password");

        Post existingPost = new Post("Title", "Body");
        existingPost.setId(1L);
        existingPost.setOwner(owner);

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(existingPost));

        assertThrows(IllegalStateException.class, () ->
                postService.deletePost(1L, "other@mail.com")
        );

        verify(postRepository).findById(1L);
        verify(postRepository, never()).delete(any(Post.class));
    }
}