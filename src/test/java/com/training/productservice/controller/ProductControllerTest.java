package com.training.productservice.controller;

import com.training.productservice.entity.Product;
import com.training.productservice.repository.ProductRepository;
import com.training.productservice.security.JwtPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private static RequestPostProcessor asUser() {
        return authenticationOf(1L, "customer@example.com", "Test Customer", "USER");
    }

    private static RequestPostProcessor asAdmin() {
        return authenticationOf(999L, "admin@example.com", "Admin User", "ADMIN");
    }

    private static RequestPostProcessor authenticationOf(Long userId, String email, String name, String role) {
        JwtPrincipal principal = new JwtPrincipal(userId, email, name, role);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }

    @Test
    void getAllProducts_returnsActiveProducts() throws Exception {
        productRepository.save(Product.builder()
                .productName("Wireless Mouse")
                .category("Electronics")
                .price(new BigDecimal("799.00"))
                .stockQuantity(150)
                .build());

        mockMvc.perform(get("/api/v1/products").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].productName").exists())
                .andExpect(jsonPath("$[0].price").exists());
    }

    @Test
    void getAllProducts_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePrice_updatesAndReturnsProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .productName("Mechanical Keyboard")
                .category("Electronics")
                .price(new BigDecimal("2500.00"))
                .stockQuantity(30)
                .build());

        mockMvc.perform(patch("/api/v1/products/{id}/price", product.getId())
                        .with(asAdmin())
                        .contentType("application/json")
                        .content("{\"price\": 2199.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.price").value(2199.00));
    }

    @Test
    void updatePrice_returns403WhenCallerNotAdmin() throws Exception {
        Product product = productRepository.save(Product.builder()
                .productName("Desk Fan")
                .category("Home")
                .price(new BigDecimal("999.00"))
                .stockQuantity(5)
                .build());

        mockMvc.perform(patch("/api/v1/products/{id}/price", product.getId())
                        .with(asUser())
                        .contentType("application/json")
                        .content("{\"price\": 899.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePrice_returns404_whenProductDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/price", UUID.randomUUID())
                        .with(asAdmin())
                        .contentType("application/json")
                        .content("{\"price\": 10.00}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePrice_returns400_whenPriceIsNegative() throws Exception {
        Product product = productRepository.save(Product.builder()
                .productName("Desk Lamp")
                .category("Home")
                .price(new BigDecimal("500.00"))
                .stockQuantity(10)
                .build());

        mockMvc.perform(patch("/api/v1/products/{id}/price", product.getId())
                        .with(asAdmin())
                        .contentType("application/json")
                        .content("{\"price\": -1.00}"))
                .andExpect(status().isBadRequest());
    }
}
