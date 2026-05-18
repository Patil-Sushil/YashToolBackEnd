package com.kalibyte.YashTools.auth.service;

import com.kalibyte.YashTools.auth.entity.RefreshToken;
import com.kalibyte.YashTools.auth.entity.User;
import com.kalibyte.YashTools.auth.repository.RefreshTokenRepository;
import com.kalibyte.YashTools.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public RefreshToken verifyRevocation(RefreshToken token) {
        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));
    }

    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        verifyExpiration(oldToken);
        verifyRevocation(oldToken);
        
        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        
        // Create new token
        return createRefreshToken(oldToken.getUser());
    }
}
