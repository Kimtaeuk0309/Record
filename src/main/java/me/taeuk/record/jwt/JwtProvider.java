package me.taeuk.record.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long EXPIRATION_MS = 1000 * 60 * 60; // 1시간 (3600000ms)

    public JwtProvider() {
        try {
            String base64EncodedSecretKey = "N3ZlYlNUdk5VbUVUWFViN2QwN3cwbHdXSDVNN1hIUnI=";
            byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            System.out.println("JwtProvider secretKey initialized successfully");
        } catch (Exception e) {
            System.err.println("JwtProvider initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("JWT secret key initialization failed", e);
        }
    }
    public String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT 토큰 검증 실패: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
