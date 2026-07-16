package com.training.productservice.service;

import com.training.productservice.dto.AvailabilityResponseDto;
import com.training.productservice.dto.PriceUpdateRequestDto;
import com.training.productservice.dto.ProductRequestDto;
import com.training.productservice.dto.ProductResponseDto;
import com.training.productservice.dto.StockUpdateRequestDto;
import com.training.productservice.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto);

    ProductResponseDto getProductById(UUID id);

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto updatePrice(UUID id, PriceUpdateRequestDto dto);

    AvailabilityResponseDto checkAvailability(UUID id, Integer quantity);

    ProductResponseDto reduceStock(UUID id, Integer quantity, String orderReference);

    void deleteProduct(UUID id);

    ProductResponseDto updateProductInfo(UUID id, ProductRequestDto product);

    ProductResponseDto adjustStock(UUID id, StockUpdateRequestDto stockUpdateRequestDto);
}
