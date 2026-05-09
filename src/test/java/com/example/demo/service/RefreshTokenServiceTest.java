package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser() {
        return new User("user@mail.com", "pwd", Set.of(Role.USER));
    }

    // --- createRefreshToken ---

    @Test
    void createRefreshToken_deletesExistingTokensForUser() {
        User user = testUser();
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void createRefreshToken_savesTokenWithCorrectUser() {
        User user = testUser();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void createRefreshToken_setsExpirySevenDaysFromNow() {
        User user = testUser();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        Instant before = Instant.now().plus(6, ChronoUnit.DAYS);
        refreshTokenService.createRefreshToken(user);
        Instant after = Instant.now().plus(8, ChronoUnit.DAYS);

        Instant expiresAt = captor.getValue().getExpiresAt();
        assertTrue(expiresAt.isAfter(before));
        assertTrue(expiresAt.isBefore(after));
    }

    @Test
    void createRefreshToken_generatesNonNullToken() {
        User user = testUser();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        assertNotNull(captor.getValue().getToken());
    }

    // --- validateRefreshToken ---

    @Test
    void validateRefreshToken_withValidToken_returnsUser() {
        User user = testUser();
        RefreshToken token = validToken(user);
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(token));

        User result = refreshTokenService.validateRefreshToken("tok");

        assertEquals(user, result);
    }

    @Test
    void validateRefreshToken_withUnknownToken_throwsResourceNotFoundException() {
        when(refreshTokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> refreshTokenService.validateRefreshToken("bad"));
    }

    @Test
    void validateRefreshToken_withRevokedToken_throwsIllegalStateException() {
        User user = testUser();
        RefreshToken token = validToken(user);
        token.setRevoked(true);
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(token));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> refreshTokenService.validateRefreshToken("tok"));
        assertEquals("Refresh token has been revoked", ex.getMessage());
    }

    @Test
    void validateRefreshToken_withExpiredToken_throwsIllegalStateException() {
        User user = testUser();
        RefreshToken token = validToken(user);
        token.setExpiresAt(Instant.now().minus(1, ChronoUnit.SECONDS));
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(token));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> refreshTokenService.validateRefreshToken("tok"));
        assertEquals("Refresh token has expired", ex.getMessage());
    }

    // --- revokeTokensForUser ---

    @Test
    void revokeTokensForUser_callsDeleteByUser() {
        User user = testUser();

        refreshTokenService.revokeTokensForUser(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    // --- helpers ---

    private RefreshToken validToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken("tok");
        token.setUser(user);
        token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        token.setRevoked(false);
        return token;
    }
}
