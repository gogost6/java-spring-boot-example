package com.georgistoilkov.catify.service;

import com.georgistoilkov.catify.dto.CreatePostRequest;
import com.georgistoilkov.catify.entity.Post;
import com.georgistoilkov.catify.entity.Role;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.repository.AuthRepository;
import com.georgistoilkov.catify.repository.CommentRepository;
import com.georgistoilkov.catify.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.georgistoilkov.catify.exception.ForbiddenException;
import com.georgistoilkov.catify.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(post1, post2)));

        Page<Post> result = postService.getAllPosts(pageable);

        assertEquals(2, result.getTotalElements());
        verify(postRepository).findAll(pageable);
    }

    @Test
    void createPost_shouldSavePostWithOwner() {
        User owner = new User("owner@mail.com", "hashed-password", Set.of(Role.USER));
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
        User owner = new User("owner@mail.com", "hashed-password", Set.of(Role.USER));

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
        User owner = new User("owner@mail.com", "hashed-password", Set.of(Role.USER));

        Post existingPost = new Post("Old title", "Old body");
        existingPost.setId(1L);
        existingPost.setOwner(owner);

        CreatePostRequest request = new CreatePostRequest("New title", "New body");

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(existingPost));

        when(authRepository.findByEmail("other@mail.com"))
                        .thenReturn(Optional.of(new User("other@mail.com", "pwd", Set.of(Role.USER))));

        assertThrows(ForbiddenException.class, () -> postService.updatePost(1L, "other@mail.com", request));

        verify(postRepository).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrow_whenPostDoesNotExist() {
        CreatePostRequest request = new CreatePostRequest("New title", "New body");

        when(postRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                                () -> postService.updatePost(999L, "owner@mail.com", request));

        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_shouldDelete_whenUserIsOwner() {
        User owner = new User("owner@mail.com", "hashed-password", Set.of(Role.USER));
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
        User owner = new User("owner@mail.com", "hashed-password", Set.of(Role.USER));

        Post existingPost = new Post("Title", "Body");
        existingPost.setId(1L);
        existingPost.setOwner(owner);

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(existingPost));

        when(authRepository.findByEmail("other@mail.com"))
                      .thenReturn(Optional.of(new User("other@mail.com", "pwd", Set.of(Role.USER))));

        assertThrows(ForbiddenException.class, () -> postService.deletePost(1L, "other@mail.com"));

        verify(postRepository).findById(1L);
        verify(postRepository, never()).delete(any(Post.class));
    }
}