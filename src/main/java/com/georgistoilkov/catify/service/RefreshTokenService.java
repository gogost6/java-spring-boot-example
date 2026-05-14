package com.georgistoilkov.catify.service;

import com.georgistoilkov.catify.entity.RefreshToken;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.exception.ResourceNotFoundException;
import com.georgistoilkov.catify.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public User validateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (token.isRevoked()) {
            throw new IllegalStateException("Refresh token has been revoked");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Refresh token has expired");
        }

        return token.getUser();
    }

    @Transactional
    public void revokeTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
