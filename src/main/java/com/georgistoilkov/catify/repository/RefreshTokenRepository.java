package com.georgistoilkov.catify.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.georgistoilkov.catify.entity.RefreshToken;
import com.georgistoilkov.catify.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
