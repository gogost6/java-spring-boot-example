package com.georgistoilkov.catify.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.georgistoilkov.catify.entity.RefreshToken;
import com.georgistoilkov.catify.entity.Role;
import com.georgistoilkov.catify.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        authRepository.deleteAll();
    }

    private User savedUser() {
        return authRepository.save(
                new User("user" + System.nanoTime() + "@mail.com", "hashed", Set.of(Role.USER))
        );
    }

    private RefreshToken savedToken(User user, String tokenValue) {
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    @Test
    void findByToken_whenExists_returnsToken() {
        User user = savedUser();
        savedToken(user, "my-token");

        Optional<RefreshToken> result = refreshTokenRepository.findByToken("my-token");

        assertTrue(result.isPresent());
        assertEquals("my-token", result.get().getToken());
    }

    @Test
    void findByToken_whenNotExists_returnsEmpty() {
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByUser_removesOnlyThatUsersTokens() {
        User user1 = savedUser();
        User user2 = savedUser();
        savedToken(user1, "token-user1");
        savedToken(user2, "token-user2");

        refreshTokenRepository.deleteByUser(user1);

        assertTrue(refreshTokenRepository.findByToken("token-user1").isEmpty());
        assertTrue(refreshTokenRepository.findByToken("token-user2").isPresent());
    }

    @Test
    void save_persistsTokenWithUser() {
        User user = savedUser();
        RefreshToken saved = savedToken(user, "abc-token");

        assertNotNull(saved.getId());
        assertEquals(user.getId(), saved.getUser().getId());
    }
}
