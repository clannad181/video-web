package com.cjym.videoweb.utils;

import com.cjym.videoweb.constant.BaseConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JWTUtil {

    // 256-bit 固定密钥
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode("PMM6r6oiYBSJ98ee3qUWQjBNPGKnl16qENfQr2GFK5g=")
    );

    private static final long accessValidity = 900000;     // 15 分钟

    @Getter
    private static final long refreshValidity = 604800000;  // 7 天

    // 生成 Access Token
    public static String createAccessToken(String serverType, String username,
                                           Collection<? extends GrantedAuthority> authorities) {
        return buildToken(serverType, username, authorities, accessValidity, BaseConstant.TOKEN_ACCESS);
    }

    // 生成 Refresh Token
    public static String createRefreshToken(String serverType, String username) {
        return buildToken(serverType, username, Collections.emptyList(), refreshValidity, BaseConstant.TOKEN_REFRESH);
    }

    private static String buildToken(String serverType, String username,
                                     Collection<? extends GrantedAuthority> authorities,
                                     long validity, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .claim("serverType", serverType)
                .claim("type", tokenType);

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
