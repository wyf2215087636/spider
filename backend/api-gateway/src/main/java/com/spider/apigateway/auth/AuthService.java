package com.spider.apigateway.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spider.apigateway.auth.dto.LoginRequest;
import com.spider.apigateway.auth.dto.LoginResponse;
import com.spider.apigateway.auth.dto.UserProfile;
import com.spider.apigateway.exception.AuthInvalidCredentialsException;
import com.spider.apigateway.exception.AuthUnauthorizedException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
    private static final int DEFAULT_EXPIRE_HOURS = 12;
    private final AppUserMapper appUserMapper;
    private final AppUserSessionMapper appUserSessionMapper;

    public AuthService(
            AppUserMapper appUserMapper,
            AppUserSessionMapper appUserSessionMapper
    ) {
        this.appUserMapper = appUserMapper;
        this.appUserSessionMapper = appUserSessionMapper;
    }

    public LoginResponse login(LoginRequest request) {
        AppUserEntity user = findByUsername(request.getUsername().trim());
        if (user == null || !"active".equals(user.getStatus())) {
            throw new AuthInvalidCredentialsException();
        }
        if (!verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new AuthInvalidCredentialsException();
        }

        AppUserSessionEntity session = new AppUserSessionEntity();
        session.setToken(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        session.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        session.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(DEFAULT_EXPIRE_HOURS));
        appUserSessionMapper.insert(session);

        return new LoginResponse(session.getToken(), toUserProfile(user));
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        appUserSessionMapper.deleteById(token);
    }

    public UserProfile me(String token) {
        return toUserProfile(resolvePrincipal(token));
    }

    public AuthPrincipal resolvePrincipal(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthUnauthorizedException();
        }
        AppUserSessionEntity session = appUserSessionMapper.selectById(token.trim());
        if (session == null) {
            throw new AuthUnauthorizedException();
        }
        if (session.getExpiresAt() == null || session.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            appUserSessionMapper.deleteById(token.trim());
            throw new AuthUnauthorizedException();
        }

        AppUserEntity user = appUserMapper.selectById(session.getUserId());
        if (user == null || !"active".equals(user.getStatus())) {
            throw new AuthUnauthorizedException();
        }
        session.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        appUserSessionMapper.updateById(session);
        return new AuthPrincipal(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }

    private AppUserEntity findByUsername(String username) {
        QueryWrapper<AppUserEntity> query = new QueryWrapper<>();
        query.eq("username", username);
        query.last("LIMIT 1");
        return appUserMapper.selectOne(query);
    }

    private boolean verifyPassword(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        // Format: pbkdf2_sha256$iterations$base64(salt)$base64(hash)
        String[] parts = passwordHash.split("\\$");
        if (parts.length != 4 || !"pbkdf2_sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, expected.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actual = skf.generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(actual, expected);
        } catch (Exception ex) {
            return false;
        }
    }

    private UserProfile toUserProfile(AppUserEntity user) {
        return new UserProfile(
                user.getId().toString(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    private UserProfile toUserProfile(AuthPrincipal principal) {
        return new UserProfile(
                principal.userId().toString(),
                principal.username(),
                principal.displayName(),
                principal.role()
        );
    }
}
