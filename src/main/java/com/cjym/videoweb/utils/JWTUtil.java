package com.cjym.videoweb.utils;

import com.cjym.videoweb.constant.BaseConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JWTUtil {

    // 256-bit 固定密钥
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode("PMM6r6oiYBSJ98ee3qUWQjBNPGKnl16qENfQr2GFK5g=")
    );

    /** 默认 access token 有效期：15 分钟（DB 无配置时兜底） */
    public static final long DEFAULT_ACCESS_VALIDITY = 900_000L;
    /** 默认 refresh token 有效期：7 天（DB 无配置时兜底） */
    public static final long DEFAULT_REFRESH_VALIDITY = 604_800_000L;

    /**
     * 生成 Access Token
     * @param accessExpiryMillis 有效期（毫秒），由 TokenExpiryConfigService 提供
     */
    public static String createAccessToken(Long userId, String serverType, String username,
                                           Collection<? extends GrantedAuthority> authorities,
                                           long accessExpiryMillis) {
        return buildToken(userId, serverType, username, authorities, accessExpiryMillis, BaseConstant.TOKEN_ACCESS);
    }

    /**
     * 生成 Refresh Token
     * @param refreshExpiryMillis 有效期（毫秒），由 TokenExpiryConfigService 提供
     */
    public static String createRefreshToken(Long userId, String serverType, String username,
                                            long refreshExpiryMillis) {
        return buildToken(userId, serverType, username, Collections.emptyList(), refreshExpiryMillis, BaseConstant.TOKEN_REFRESH);
    }

    private static String buildToken(Long userId, String serverType, String username,
                                     Collection<? extends GrantedAuthority> authorities,
                                     long validity, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .claim("serverType", serverType)
                .claim("type", tokenType)
                .claim("userId",userId);

        if ("access".equals(tokenType)) {
            builder.claim("auth", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(",")));
        }

        return builder.signWith(SIGNING_KEY).compact();
    }

    // 通用解析
    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public static String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    public String getTokenServerType(String token) {
        return parseClaims(token).get("serverType", String.class);
    }

    /** 静态便捷方法：从 token 中提取 serverType */
    public static String getServerType(String token) {
        return parseClaims(token).get("serverType", String.class);
    }

    // 从 token 中提取权限（仅 Access Token 有效）
    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        Claims claims = parseClaims(token);
        String authString = claims.get("auth", String.class);
        if (authString == null || authString.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(authString.split(","))
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .toList();
    }
}
