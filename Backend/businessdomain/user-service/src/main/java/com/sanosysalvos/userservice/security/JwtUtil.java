package com.sanosysalvos.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.expiration}") private Long expiration;

    private Key getSigningKey() { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generateToken(UserDetails userDetails, Long userId, String role) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", userId); claims.put("role", role);
        return Jwts.builder().setClaims(claims).setSubject(String.valueOf(userId))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try { return !extractAllClaims(token).getExpiration().before(new Date()); } catch (Exception e) { return false; }
    }
}
