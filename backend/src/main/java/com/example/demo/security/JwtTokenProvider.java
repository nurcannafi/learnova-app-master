package com.example.demo.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final String secret;
    private final long jwtExpirationMs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtTokenProvider(
            @Value("${app.jwt.secret:LearnovaSuperSecretKey2026SecureJwtAuthenticationKeyLongString}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.secret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return generateTokenFromUser(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getRole().name());
    }

    public String generateTokenFromUser(Long userId, String email, String role) {
        long now = System.currentTimeMillis();
        long expiry = now + jwtExpirationMs;

        try {
            // Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8));

            // Claims (Payload)
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", email);
            claims.put("id", userId);
            claims.put("role", role);
            claims.put("iat", now / 1000);
            claims.put("exp", expiry / 1000);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsString(claims).getBytes(StandardCharsets.UTF_8));

            // Signature
            String dataToSign = encodedHeader + "." + encodedPayload;
            String signature = sign(dataToSign);

            return dataToSign + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("JWT token yaradılarkən xəta baş verdi", e);
        }
    }

    public String getEmailFromToken(String token) {
        JsonNode claims = getClaims(token);
        return claims != null && claims.has("sub") ? claims.get("sub").asText() : null;
    }

    public Long getUserIdFromToken(String token) {
        JsonNode claims = getClaims(token);
        return claims != null && claims.has("id") ? claims.get("id").asLong() : null;
    }

    public String getRoleFromToken(String token) {
        JsonNode claims = getClaims(token);
        return claims != null && claims.has("role") ? claims.get("role").asText() : null;
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(dataToSign);

            if (!expectedSignature.equals(parts[2])) {
                return false;
            }

            // Check expiration
            JsonNode claims = getClaims(token);
            if (claims == null || !claims.has("exp")) {
                return false;
            }
            long expSeconds = claims.get("exp").asLong();
            long currentSeconds = System.currentTimeMillis() / 1000;
            return currentSeconds < expSeconds;
        } catch (Exception e) {
            return false;
        }
    }

    private JsonNode getClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readTree(payloadBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }
}
