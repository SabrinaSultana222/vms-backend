package com.vms.backend.security;

import tools.jackson.databind.ObjectMapper;
import com.vms.backend.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            sendUnauthorized(response, request.getRequestURI(), "Invalid or expired token");
            return;
        }

        String subject = jwtService.extractSubject(token);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String path, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                path
        );
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
