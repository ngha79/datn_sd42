package com.base.service;

import com.base.entity.RefreshToken;
import com.base.entity.User;
import com.base.exception.TokenRefreshException;
import com.base.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        Optional<RefreshToken> existing =
                refreshTokenRepository.findByUser(user);

        RefreshToken token;

        if (existing.isPresent()) {
            token = existing.get();

            token.setToken(UUID.randomUUID().toString());
            token.setExpiryDate(
                    Instant.now().plusMillis(refreshTokenDurationMs)
            );
        } else {
            token = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(
                            Instant.now().plusMillis(refreshTokenDurationMs)
                    )
                    .build();
        }

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please login again.");
        }
        return token;
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token not found"));
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
