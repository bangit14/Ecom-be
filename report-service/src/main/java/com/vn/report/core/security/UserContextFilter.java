package com.vn.report.core.security;

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

@Component
@Slf4j(topic = "Report-UserFilter")
public class UserContextFilter extends OncePerRequestFilter {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String xUserId = request.getHeader("X-User-Id");
            String xFullName = request.getHeader("X-User-Full-Name");

            if (StringUtils.hasText(xUserId)) {
                try {
                    UserContext.setUserId(Long.parseLong(xUserId));
                } catch (NumberFormatException ignored) {}
            }
            if (StringUtils.hasText(xFullName)) {
                UserContext.setFullName(xFullName);
            }

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
                        if (userIdObj != null) {
                            UserContext.setUserId(Long.parseLong(String.valueOf(userIdObj)));
                        }
                        String fullName = (String) claims.get("fullName");
                        if (fullName != null) {
                            UserContext.setFullName(fullName);
                        }
                    } catch (Exception e) {
                        log.debug("Could not parse JWT token in report-service: {}", e.getMessage());
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
