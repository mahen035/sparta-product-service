package com.training.productservice.service;

import com.training.productservice.model.ProductRequest;
import com.training.productservice.model.ProductResponseDto;

import java.util.UUID;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequest dto);

    String deleteProduct(UUID id);
}
