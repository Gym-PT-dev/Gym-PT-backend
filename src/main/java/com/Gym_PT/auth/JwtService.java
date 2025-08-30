package com.Gym_PT.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final byte[] key;
    private final long accessExp;   // seconds
    private final long refreshExp;  // seconds

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access.exp}") long accessExp,
                      @Value("${jwt.refresh.exp}") long refreshExp) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.accessExp = accessExp;
        this.refreshExp = refreshExp;
    }

    public String createAccess(String subject) { return create(subject, accessExp); }
    public String createRefresh(String subject) { return create(subject, refreshExp); }

    private String create(String subject, long seconds) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }

    public String parseSubject(String jwt) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(key))
                .build()
                .parseSignedClaims(jwt)
                .getPayload().getSubject();
    }
}