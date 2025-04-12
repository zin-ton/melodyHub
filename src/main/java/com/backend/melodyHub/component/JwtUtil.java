package com.backend.melodyHub.component;

import com.backend.melodyHub.model.User;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {
    private final String SECRET;
    private final long EXPIRATION_TIME = 1000 * 60 * 60;
    private final Key key;
    Dotenv dotenv = Dotenv.load();

    public JwtUtil() {
        SECRET = Optional.ofNullable(dotenv.get("256BIT_SECRET"))
                .orElseThrow(() -> new IllegalStateException("256BIT_SECRET environment variable is not set"));
        key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getLogin())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public TokenValidationResult validateTokenFull(String token) {
        if (token == null || token.isBlank()) {
            return TokenValidationResult.error("Token cannot be empty");
        }

        String tokenRegex = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$";
        if (!token.matches(tokenRegex)) {
            return TokenValidationResult.error("Invalid JWT token format");
        }

        if (!validateToken(token)) {
            return TokenValidationResult.error("Invalid token");
        }

        return TokenValidationResult.success();
    }

}
