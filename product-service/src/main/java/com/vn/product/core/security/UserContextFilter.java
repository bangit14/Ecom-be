package com.vn.product.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j(topic = "Product-UserFilter")
public class UserContextFilter extends OncePerRequestFilter {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    private UUID parseUuidSafe(String raw) {
        if (!StringUtils.hasText(raw)) return null;
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Check headers from API Gateway
            String xUserId = request.getHeader("X-User-Id");
            String xStoreId = request.getHeader("X-Store-Id");
            String xFullName = request.getHeader("X-User-Full-Name");
            String xEmail = request.getHeader("X-User-Email");
            String xRoles = request.getHeader("X-User-Roles");

            if (StringUtils.hasText(xUserId)) {
                UserContext.setUserId(parseUuidSafe(xUserId));
            }
            if (StringUtils.hasText(xStoreId)) {
                UserContext.setStoreId(parseUuidSafe(xStoreId));
            }
            if (StringUtils.hasText(xFullName)) {
                UserContext.setFullName(xFullName);
            }
            if (StringUtils.hasText(xEmail)) {
                UserContext.setUserEmail(xEmail);
            }
            if (StringUtils.hasText(xRoles)) {
                UserContext.setUserRoles(xRoles);
            }

            // 2. Fallback read from Authorization Bearer Token
            if (UserContext.getUserId() == null) {
                String authHeader = request.getHeader("Authorization");
                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        Claims claims = Jwts.parser()
                                .verifyWith(getSigningKey())
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                        Object userIdObj = claims.get("userId");
                        if (userIdObj == null) {
                            userIdObj = claims.getSubject();
                        }
                        if (userIdObj != null) {
                            UserContext.setUserId(parseUuidSafe(String.valueOf(userIdObj)));
                        }

                        Object storeIdObj = claims.get("storeId");
                        if (storeIdObj != null) {
                            UserContext.setStoreId(parseUuidSafe(String.valueOf(storeIdObj)));
                        }

                        String fullName = (String) claims.get("fullName");
                        if (fullName != null) {
                            UserContext.setFullName(fullName);
                        }

                        String email = (String) claims.get("email");
                        if (email != null) {
                            UserContext.setUserEmail(email);
                        }

                        Object roleObj = claims.get("role");
                        if (roleObj != null) {
                            UserContext.setUserRoles(String.valueOf(roleObj));
                        }
                    } catch (Exception e) {
                        log.debug("Could not parse JWT token in product-service: {}", e.getMessage());
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
