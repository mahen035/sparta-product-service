package com.training.productservice.service;

import com.training.productservice.dto.AvailabilityResponseDto;
import com.training.productservice.dto.PriceUpdateRequestDto;
import com.training.productservice.dto.ProductRequestDto;
import com.training.productservice.dto.ProductResponseDto;
import com.training.productservice.dto.StockUpdateRequestDto;
import com.training.productservice.entity.Product;
import com.training.productservice.exception.DuplicateProductException;
import com.training.productservice.exception.InsufficientStockException;
import com.training.productservice.exception.ProductNotFoundException;
import com.training.productservice.repository.ProductRepository;
import com.training.productservice.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        if (productRepository.existsByProductNameIgnoreCaseAndCategoryIgnoreCase(dto.getProductName(), dto.getCategory())) {
            throw new DuplicateProductException(
                    "A product named '" + dto.getProductName() + "' already exists in category '" + dto.getCategory() + "'");
        }
        Product saved = productRepository.save(productMapper.toEntity(dto));
        log.info("Product created with id={}", saved.getId());
        return productMapper.toResponseDto(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id) {
        return productMapper.toResponseDto(findProductOrThrow(id));
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto dto) {
        Product product = findProductOrThrow(id);
        productMapper.updateEntity(product, dto);
        return productMapper.toResponseDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponseDto updatePrice(UUID id, PriceUpdateRequestDto dto) {
        Product product = findProductOrThrow(id);
        product.setPrice(dto.getPrice());
        return productMapper.toResponseDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponseDto updateStock(UUID id, StockUpdateRequestDto dto) {
        Product product = findProductOrThrow(id);
        int current = product.getStockQuantity();
        int updated = switch (dto.getOperation()) {
            case INCREASE -> current + dto.getQuantity();
            case DECREASE -> current - dto.getQuantity();
            case SET -> dto.getQuantity();
        };
        if (updated < 0) {
            throw new InsufficientStockException(
                    "Cannot decrease stock of product " + id + " by " + dto.getQuantity() + "; current stock is " + current);
        }
        product.setStockQuantity(updated);
        return productMapper.toResponseDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkAvailability(UUID id, Integer quantity) {
        Product product = findProductOrThrow(id);
        return AvailabilityResponseDto.builder()
                .productId(product.getId())
                .requestedQuantity(quantity)
                .available(product.getStockQuantity() >= quantity)
                .currentStock(product.getStockQuantity())
                .build();
    }

    @Override
    @Transactional
    public ProductResponseDto reduceStock(UUID id, Integer quantity, String orderReference) {
        Product product = findProductOrThrow(id);
        if (product.getStockQuantity() < quantity) {
            log.warn("Insufficient stock for productId={} orderReference={} requested={} available={}",
                    id, orderReference, quantity, product.getStockQuantity());
            throw new InsufficientStockException(
                    "Insufficient stock for product " + id + ": requested " + quantity + ", available " + product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        Product saved = productRepository.save(product);
        log.info("Stock reduced for productId={} by quantity={} due to orderReference={}", id, quantity, orderReference);
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }
}
