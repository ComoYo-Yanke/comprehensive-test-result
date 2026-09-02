package com.zongce.comprehensive.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * <p>基于 jjwt 0.12.x，负责生成与解析 token。</p>
 */
@Component
public class JwtUtil {

    /** 签名密钥 */
    @Value("${zongce.jwt.secret}")
    private String secret;

    /** 有效期（毫秒） */
    @Value("${zongce.jwt.expire:86400000}")
    private long expire;

    /** 生成签名密钥 */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     *
     * @param user 当前用户信息
     * @return JWT 字符串
     */
    public String generateToken(CurrentUser user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("userType", user.getUserType())
                .claim("role", user.getRole())
                .claim("username", user.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token，校验签名与有效期
     *
     * @param token JWT 字符串
     * @return 解析出的用户信息
     */
    public CurrentUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        String userType = claims.get("userType", String.class);
        Integer role = claims.get("role", Integer.class);
        String username = claims.get("username", String.class);
        return new CurrentUser(userId, userType, role, username);
    }
}
