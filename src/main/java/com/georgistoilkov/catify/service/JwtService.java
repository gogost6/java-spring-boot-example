package com.georgistoilkov.catify.service;

import com.georgistoilkov.catify.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(@Value("${jwt.secret}") String secret) {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> "ROLE_" + role.name())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("demo-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60 * 60))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}