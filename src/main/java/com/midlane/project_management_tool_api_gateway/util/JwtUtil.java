package com.midlane.project_management_tool_api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
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
            if (rsaPublicKeyString == null || rsaPublicKeyString.trim().isEmpty()) {
                logger.error("RSA public key is not configured");
                throw new RuntimeException("RSA public key is not configured");
            }
            this.publicKey = decodePublicKey(rsaPublicKeyString);
            logger.info("RSA public key loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load RSA public key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize JWT utility", e);
        }
    }

    private PublicKey decodePublicKey(String keyString) throws Exception {
        // Handle different newline formats and clean up the key string
        String publicKeyPEM = keyString
                .replace("\\n", "\n")  // Handle escaped newlines
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");  // Remove all whitespace including newlines

        logger.debug("Processed public key length: {}", publicKeyPEM.length());

        // Decode the Base64 encoded string
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        // Create the public key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
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
            JwtParser parser = Jwts.parser()
                    .verifyWith(publicKey)
                    .build();
            return parser.parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            logger.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractRole(String token) {
        try {
            return extractClaim(token, claims -> claims.get("role", String.class));
        } catch (Exception e) {
            logger.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }
}
