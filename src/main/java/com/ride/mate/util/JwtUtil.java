package com.ride.mate.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility
 * Utility class for JWT token generation, validation, and parsing
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 09-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 09-03-2026    N/A          N/A          Tishan          Added userName claim support
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    /**
     * Generate access token for user
     *
     * @param userId user ID
     * @param email user email
     * @param role user role
     * @param userName user's full name
     * @return JWT access token
     */
    public String generateAccessToken(Long userId, String email, String role, String userName) {
        log.info("Generating access token for user: {}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("userName", userName);
        claims.put("tokenType", "ACCESS");
        return createToken(claims, email, accessTokenExpiration);
    }

    /**
     * Generate refresh token for user
     *
     * @param userId user ID
     * @param email user email
     * @return JWT refresh token
     */
    public String generateRefreshToken(Long userId, String email) {
        log.info("Generating refresh token for user: {}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenType", "REFRESH");
        return createToken(claims, email, refreshTokenExpiration);
    }

    /**
     * Create JWT token with claims
     *
     * @param claims token claims
     * @param subject token subject (email)
     * @param expiration token expiration time in milliseconds
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get signing key from secret
     *
     * @return SecretKey for signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract email from token
     *
     * @param token JWT token
     * @return email (subject)
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     *
     * @param token JWT token
     * @return user ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract role from token
     *
     * @param token JWT token
     * @return user role
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract username from token
     *
     * @param token JWT token
     * @return user's full name
     */
    public String extractUserName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userName", String.class);
    }

    /**
     * Extract token type from token
     *
     * @param token JWT token
     * @return token type (ACCESS or REFRESH)
     */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * Extract expiration date from token
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from token
     *
     * @param token JWT token
     * @param claimsResolver function to extract claim
     * @param <T> claim type
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     *
     * @param token JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Validate token against user email
     *
     * @param token JWT token
     * @param email user email
     * @return true if valid
     */
    public Boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token structure and signature
     *
     * @param token JWT token
     * @return true if token is valid
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is a refresh token
     *
     * @param token JWT token
     * @return true if refresh token
     */
    public Boolean isRefreshToken(String token) {
        String tokenType = extractTokenType(token);
        return "REFRESH".equals(tokenType);
    }

    /**
     * Check if token is an access token
     *
     * @param token JWT token
     * @return true if access token
     */
    public Boolean isAccessToken(String token) {
        String tokenType = extractTokenType(token);
        return "ACCESS".equals(tokenType);
    }
}

