package com.georgistoilkov.catify.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.georgistoilkov.catify.config.SecurityConfig;
import com.georgistoilkov.catify.dto.CommentResponse;
import com.georgistoilkov.catify.exception.ForbiddenException;
import com.georgistoilkov.catify.exception.ResourceNotFoundException;
import com.georgistoilkov.catify.service.CommentService;

@WebMvcTest(controllers = CommentController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CommentService commentService;

    private CommentResponse sampleComment() {
        return new CommentResponse(1L, "Hello world", "user@mail.com", null, null);
    }

    @Test
    void getCommentsByPostId_returnsPagedComments() {
        when(commentService.getCommentsByPostId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleComment())));

        mvc.get().uri("/api/comment/1/post")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].content").isEqualTo("Hello world");
    }

    @Test
    void getCommentsByPostId_withUnknownPost_returns404() {
        when(commentService.getCommentsByPostId(eq(99L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Post not found"));

        mvc.get().uri("/api/comment/99/post")
                .assertThat()
                .hasStatus(404);
    }

    @Test
    void getCommentById_returnsComment() {
        when(commentService.getCommentById(1L)).thenReturn(sampleComment());

        mvc.get().uri("/api/comment/1")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content").isEqualTo("Hello world");
    }

    @Test
    void getCommentById_withUnknownId_returns404() {
        when(commentService.getCommentById(99L))
                .thenThrow(new ResourceNotFoundException("Comment not found"));

        mvc.get().uri("/api/comment/99")
                .assertThat()
                .hasStatus(404);
    }

    @Test
    void addComment_authenticated_returnsComment() {
        when(commentService.create(eq(1L), eq("user@mail.com"), eq("Hello world")))
                .thenReturn(sampleComment());

        mvc.post().uri("/api/comment/1")
                .with(jwt().jwt(jwt -> jwt.subject("user@mail.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "content": "Hello world" }
                        """)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content").isEqualTo("Hello world");
    }

    @Test
    void addComment_unauthenticated_returns401() {
        mvc.post().uri("/api/comment/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "content": "Hello world" }
                        """)
                .assertThat()
                .hasStatus(401);
    }

    @Test
    void addComment_withBlankContent_returns400() {
        mvc.post().uri("/api/comment/1")
                .with(jwt().jwt(jwt -> jwt.subject("user@mail.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "content": "" }
                        """)
                .assertThat()
                .hasStatus(400);
    }

    @Test
    void updateComment_owner_returnsUpdated() {
        CommentResponse updated = new CommentResponse(1L, "Updated", "user@mail.com", null, null);
        when(commentService.update(eq(1L), eq("user@mail.com"), eq("Updated")))
                .thenReturn(updated);

        mvc.put().uri("/api/comment/1")
                .with(jwt().jwt(jwt -> jwt.subject("user@mail.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "content": "Updated" }
                        """)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content").isEqualTo("Updated");
    }

    @Test
    void updateComment_nonOwner_returns403() {
        when(commentService.update(eq(1L), eq("other@mail.com"), any()))
                .thenThrow(new ForbiddenException("You are not the owner of this comment!"));

        mvc.put().uri("/api/comment/1")
                .with(jwt().jwt(jwt -> jwt.subject("other@mail.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "content": "Hacked" }
                        """)
                .assertThat()
                .hasStatus(403);
    }

    @Test
    void updateComment_unauthenticated_returns401() {
        mvc.put().uri("/api/comment/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "content": "Updated" }
                        """)
                .assertThat()
                .hasStatus(401);
    }

    @Test
    void deleteComment_owner_returns200() {
        doNothing().when(commentService).delete(eq(1L), eq("user@mail.com"));

        mvc.delete().uri("/api/comment/1")
                .with(jwt().jwt(jwt -> jwt.subject("user@mail.com")))
                .assertThat()
                .hasStatusOk();
    }

    @Test
    void deleteComment_nonOwner_returns403() {
        doThrow(new ForbiddenException("You are not the owner of this comment!"))
                .when(commentService).delete(eq(1L), eq("other@mail.com"));

        mvc.delete().uri("/api/comment/1")
                .with(jwt().jwt(jwt -> jwt.subject("other@mail.com")))
                .assertThat()
                .hasStatus(403);
    }

    @Test
    void deleteComment_unauthenticated_returns401() {
        mvc.delete().uri("/api/comment/1")
                .assertThat()
                .hasStatus(401);
    }
}
