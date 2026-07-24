package com.training.productservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.productservice.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

/** Consistent JSON 401/403 bodies (matching ErrorResponseDto) instead of Spring Security's default plain-text/redirect behavior. */
public final class JsonAuthEntryPoints {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private JsonAuthEntryPoints() {
    }

    public static AuthenticationEntryPoint unauthorized() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) ->
                write(response, HttpStatus.UNAUTHORIZED, "Authentication required", request.getRequestURI());
    }

    public static AccessDeniedHandler forbidden() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) ->
                write(response, HttpStatus.FORBIDDEN, "Access is denied", request.getRequestURI());
    }

    private static void write(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponseDto body = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
        response.getWriter().write(MAPPER.writeValueAsString(body));
    }
}
