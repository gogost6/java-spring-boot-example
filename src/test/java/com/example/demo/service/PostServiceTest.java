package com.example.demo.service;

import com.example.demo.entity.Post;
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
    void createPost_shouldSavePost() {
        Post post = new Post("Title", "Body");
        post.setId(1L);
        Post savedPost = new Post("Title", "Body");
        savedPost.setId(2L);

        when(postRepository.save(post)).thenReturn(savedPost);

        Post result = postService.createPost(post);

        assertNotNull(result.getId());
        assertEquals("Title", result.getTitle());
        verify(postRepository).save(post);
    }

    @Test
    void updatePost_shouldUpdatePost_whenPostExists() {
        Post existingPost = new Post("Old title", "Old body");
        existingPost.setId(1L);

        Post updatedPost = new Post("New title", "New body");

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(existingPost)).thenReturn(existingPost);

        Optional<Post> result = postService.updatePost(1L, updatedPost);

        assertTrue(result.isPresent());
        assertEquals("New title", result.get().getTitle());
        assertEquals("New body", result.get().getBody());

        verify(postRepository).findById(1L);
        verify(postRepository).save(existingPost);
    }

    @Test
    void updatePost_shouldReturnEmpty_whenPostDoesNotExist() {
        Post updatedPost = new Post("New title", "New body");

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Post> result = postService.updatePost(999L, updatedPost);

        assertTrue(result.isEmpty());
        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_shouldDeleteAndReturnTrue_whenPostExists() {
        when(postRepository.existsById(1L)).thenReturn(true);

        boolean result = postService.deletePost(1L);

        assertTrue(result);
        verify(postRepository).existsById(1L);
        verify(postRepository).deleteById(1L);
    }

    @Test
    void deletePost_shouldReturnFalse_whenPostDoesNotExist() {
        when(postRepository.existsById(999L)).thenReturn(false);

        boolean result = postService.deletePost(999L);

        assertFalse(result);
        verify(postRepository).existsById(999L);
        verify(postRepository, never()).deleteById(anyLong());
    }
}