package com.midlane.project_management_tool_api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${rsa.public.key}")
    private String rsaPublicKeyString;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.publicKey = decodePublicKey(rsaPublicKeyString);
            logger.info("RSA public key loaded successfully for JWT validation");
        } catch (Exception e) {
            logger.error("Failed to load RSA public key: {}", e.getMessage());
            throw new IllegalStateException("Could not initialize RSA public key for JWT validation", e);
        }
    }

    private PublicKey decodePublicKey(String publicKeyString) throws Exception {
        // Remove PEM header/footer and whitespace
        String publicKeyPEM = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // Decode base64
        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);

        // Generate public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public String extractTokenType(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("tokenType", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.warn("Failed to parse JWT claims: {}", e.getMessage());
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't check
        }
    }

    /**
     * Validate RSA JWT token - checks signature, expiration, and token type
     */
    public Boolean validateToken(String token) {
        try {
            // Parse and validate signature (this will throw exception if invalid)
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);

            // Additional validation
            if (isTokenExpired(token)) {
                logger.debug("Token is expired");
                return false;
            }

            // Ensure it's an access token (not refresh token)
            String tokenType = extractTokenType(token);
            if (!"ACCESS".equals(tokenType)) {
                logger.debug("Token type is not ACCESS: {}", tokenType);
                return false;
            }

            logger.debug("Token validation successful");
            return true;
        } catch (Exception e) {
            logger.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token for specific username
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && validateToken(token);
        } catch (Exception e) {
            logger.debug("JWT token validation failed for username {}: {}", username, e.getMessage());
            return false;
        }
    }
}
