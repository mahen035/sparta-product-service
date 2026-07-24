package com.training.productservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the inbound request's Authorization header onto every outbound
 * Feign call (e.g. to order-service), so a downstream JWT-secured endpoint
 * sees the same caller that hit this service. Only safe for Feign calls made
 * synchronously on the original request thread.
 */
@Configuration
public class FeignAuthPropagationConfig {

    @Bean
    public RequestInterceptor authorizationHeaderForwardingInterceptor() {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return;
            }
            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
            }
        };
    }
}
