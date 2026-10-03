package com.vn.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j(topic = "Gateway-AuthFilter")
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Các endpoint PUBLIC - không cần JWT
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            // Auth - đăng ký, đăng nhập, xác thực
            "/api/auth/login",
            "/api/auth/login/google",
            "/api/auth/register",
            "/api/auth/register/google",
            "/api/auth/refresh",
            "/api/auth/verify-code",
            "/api/auth/resend-verification",
            "/api/auth/resend-code",
            "/api/auth/forgot-password",
            "/api/auth/authenticate"
    );

    /**
     * Các path cho phép GET public (xem sản phẩm, danh mục, đánh giá)
     * nhưng POST/PUT/DELETE vẫn cần auth
     */
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/products/**",
            "/api/categories/**",
            "/api/reviews/**"
    );

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Public paths - cho qua không cần token
        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        // 2. Public GET paths - chỉ GET mới public
        if (HttpMethod.GET.equals(method) && isPublicGetPath(path)) {
            log.debug("Public GET path accessed: {}", path);
            return chain.filter(exchange);
        }

        // 3. Kiểm tra Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {} {}", method, path);
            return onUnauthorized(exchange, "Vui lòng đăng nhập để tiếp tục");
        }

        // 4. Validate JWT và enrich headers
        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? String.valueOf(userIdObj) : "";
            String email = (String) claims.get("email");
            Object rolesObj = claims.get("roles");
            String roles = rolesObj != null ? rolesObj.toString() : "";
            String fullName = (String) claims.get("fullName");

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-User-Roles", roles)
                    .header("X-User-Full-Name", fullName != null ? fullName : "")
                    .build();

            log.debug("Authenticated request: {} {} | userId={}", method, path, userId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.warn("JWT validation failed for {} {}: {}", method, path, e.getMessage());
            return onUnauthorized(exchange, "Token không hợp lệ hoặc đã hết hạn");
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private boolean isPublicGetPath(String path) {
        return PUBLIC_GET_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
