package com.training.productservice.security;

/** The authenticated identity extracted from a validated access token's claims. */
public record JwtPrincipal(Long userId, String email, String name, String role) {
}
