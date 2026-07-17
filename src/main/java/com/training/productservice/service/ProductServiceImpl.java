package com.training.productservice.service;

import com.training.productservice.entity.Product;
import com.training.productservice.enums.ProductStatus;
import com.training.productservice.exception.DuplicateProductException;
import com.training.productservice.exception.InsufficientStockException;
import com.training.productservice.exception.ProductNotFoundException;
import com.training.productservice.model.ProductRequest;
import com.training.productservice.model.ProductResponseDto;
import com.training.productservice.repository.ProductRepository;
import com.training.productservice.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequest dto) {
        if (productRepository.existsByProductNameIgnoreCaseAndCategoryIgnoreCase(dto.getProductName(), dto.getCategory())) {
            throw new DuplicateProductException(
                    "A product named '" + dto.getProductName() + "' already exists in category '" + dto.getCategory() + "'");
        }
        Product saved = productRepository.save(productMapper.toEntity(dto));
        log.info("Product created with id={}", saved.getId());
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public String deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);

        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new InsufficientStockException(
                    "Product '" + product.getProductName() + "' is already deleted."
            );
        }

        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
        return product.getProductName() + "was deleted";
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

}
