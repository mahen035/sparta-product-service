package com.training.productservice.util;

import com.training.productservice.dto.ProductRequestDto;
import com.training.productservice.dto.ProductResponseDto;
import com.training.productservice.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto dto) {
        return Product.builder()
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .active(true)
                .build();
    }

    public ProductResponseDto toResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public void updateEntity(Product product, ProductRequestDto dto) {
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
    }


}
