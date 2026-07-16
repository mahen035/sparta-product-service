package com.training.productservice.controller;

import com.training.productservice.entity.Product;
import com.training.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Test
    void getAllProducts_returnsActiveProducts() throws Exception {
        productRepository.save(Product.builder()
                .productName("Wireless Mouse")
                .category("Electronics")
                .price(new BigDecimal("799.00"))
                .stockQuantity(150)
                .build());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].productName").exists())
                .andExpect(jsonPath("$[0].price").exists());
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
                        .contentType("application/json")
                        .content("{\"price\": 2199.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.price").value(2199.00));
    }

    @Test
    void updatePrice_returns404_whenProductDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/price", UUID.randomUUID())
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
                        .contentType("application/json")
                        .content("{\"price\": -1.00}"))
                .andExpect(status().isBadRequest());
    }
}
