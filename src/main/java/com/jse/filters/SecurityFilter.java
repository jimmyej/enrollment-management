package com.jse.filters;

import com.jse.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SecurityFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Autowired
    public SecurityFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/api/v1/auth") || path.contains("swagger") || path.contains("api-docs") || path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        var authHeaders = exchange.getRequest().getHeaders().getOrEmpty("Authorization");
        if (authHeaders.isEmpty() || !authHeaders.getFirst().startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = authHeaders.getFirst().substring(7);
        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String username = jwtUtil.getUsernameFromToken(token);
        var roles = jwtUtil.getRolesFromToken(token);

        ServerHttpRequest mutatedReq = exchange.getRequest().mutate()
                .header("user", username)
                .header("roles", String.join(",", roles))
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedReq).build();

        String requestPath = exchange.getRequest().getURI().getPath();
        if (requestPath.startsWith("/api/v1/students") || requestPath.startsWith("/api/v1/courses") || requestPath.startsWith("/api/v1/enrollments")) {
            mutatedExchange.getResponse().getHeaders().add("Authorization", "Bearer " + token);
            mutatedExchange.getResponse().getHeaders().add("Access-Control-Expose-Headers", "Authorization");
        }

        return chain.filter(mutatedExchange);
    }
}
