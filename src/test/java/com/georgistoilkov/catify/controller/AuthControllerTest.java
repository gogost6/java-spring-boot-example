package com.georgistoilkov.catify.controller;

import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.georgistoilkov.catify.config.SecurityConfig;
import com.georgistoilkov.catify.entity.RefreshToken;
import com.georgistoilkov.catify.entity.Role;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.exception.ResourceNotFoundException;
import com.georgistoilkov.catify.service.AuthService;
import com.georgistoilkov.catify.service.JwtService;
import com.georgistoilkov.catify.service.RefreshTokenService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    void refresh_withValidToken_returnsNewTokenPair() {
        User user = new User("user@mail.com", "pwd", Set.of(Role.USER));
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");

        when(refreshTokenService.validateRefreshToken("valid-token")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);

        mvc.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "refreshToken": "valid-token" }
                        """)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.token").isEqualTo("new-access-token");
    }

    @Test
    void refresh_withValidToken_returnsNewRefreshToken() {
        User user = new User("user@mail.com", "pwd", Set.of(Role.USER));
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");

        when(refreshTokenService.validateRefreshToken("valid-token")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);

        mvc.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "refreshToken": "valid-token" }
                        """)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.refreshToken").isEqualTo("new-refresh-token");
    }

    @Test
    void refresh_withUnknownToken_returns404() {
        when(refreshTokenService.validateRefreshToken("unknown-token"))
                .thenThrow(new ResourceNotFoundException("Refresh token not found"));

        mvc.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "refreshToken": "unknown-token" }
                        """)
                .assertThat()
                .hasStatus(404);
    }

    @Test
    void refresh_withExpiredToken_returns409() {
        when(refreshTokenService.validateRefreshToken("expired-token"))
                .thenThrow(new IllegalStateException("Refresh token has expired"));

        mvc.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "refreshToken": "expired-token" }
                        """)
                .assertThat()
                .hasStatus(409);
    }

    @Test
    void refresh_withRevokedToken_returns409() {
        when(refreshTokenService.validateRefreshToken("revoked-token"))
                .thenThrow(new IllegalStateException("Refresh token has been revoked"));

        mvc.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "refreshToken": "revoked-token" }
                        """)
                .assertThat()
                .hasStatus(409);
    }

    @Test
    void refresh_withBlankToken_returns400() {
        mvc.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "refreshToken": "" }
                        """)
                .assertThat()
                .hasStatus(400);
    }
}
