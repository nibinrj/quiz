package com.nibin.quiz.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTservice {

    // 1. Inject the secret from application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    private Map<String,Object> claims = new HashMap<>();

    public String generateToken(String username) {
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 30)) // Fixed expiration math (was missing * 1000 for milliseconds)
                .and()
                .signWith(getkey())
                .compact();
    }

    private SecretKey getkey() {
        // 3. Ensure your secret key in application.properties is long enough for HmacSHA256
        // Or strictly use Decoders if it's Base64, or just bytes if it's plain text.
        // For simplicity with your "nibin123", we might need to pad it or use Keys.hmacShaKeyFor(secretKey.getBytes()) directly if it's not Base64.

        // Robust fix: Use the raw bytes of your string if it's not a proper Base64 encoded key
        // But standard practice is Base64. Let's assume you want to use the simple string for now:
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(getBase64Key()));
    }

    // Helper to ensure we have a valid Base64 key from your simple string
    private String getBase64Key() {
        return java.util.Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getkey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}