package com.training.productservice.service;

import com.training.productservice.client.OrderServiceClient;
import com.training.productservice.dto.*;
import com.training.productservice.entity.Product;
import com.training.productservice.enums.ProductAvailability;
import com.training.productservice.enums.ProductStatus;
import com.training.productservice.event.ProductEventPublisher;
import com.training.productservice.event.ProductEventType;
import com.training.productservice.exception.DuplicateProductException;
import com.training.productservice.exception.InsufficientStockException;
import com.training.productservice.exception.ProductHasOpenOrdersException;
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
    private final OrderServiceClient orderServiceClient;
    private final ProductEventPublisher productEventPublisher;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        if (productRepository.existsByProductNameIgnoreCaseAndCategoryIgnoreCase(dto.getProductName(), dto.getCategory())) {
            throw new DuplicateProductException(
                    "A product named '" + dto.getProductName() + "' already exists in category '" + dto.getCategory() + "'");
        }
        Product saved = productRepository.save(productMapper.toEntity(dto));
        log.info("Product created with id={}", saved.getId());
        productEventPublisher.publish(ProductEventType.PRODUCT_CREATED, saved);
        return productMapper.toResponseDto(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findByStatusNot(ProductStatus.DISCONTINUED).stream()
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
    public ProductResponseDto updatePrice(UUID id, PriceUpdateRequestDto dto) {
        Product product = findProductOrThrow(id);
        product.setPrice(dto.getPrice());
        Product saved = productRepository.save(product);
        productEventPublisher.publish(ProductEventType.PRODUCT_PRICE_CHANGED, saved);
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkAvailability(UUID id, Integer quantity) {
        Product product = productRepository.findById(id).orElseThrow(() ->
        {
            log.warn("The Product with ID: " + id + " does not exists, kindly enter valid ID.");
            throw new ProductNotFoundException("The Product with ID: " + id + " does not exists, kindly enter valid ID.");
        });

        // Quantity Check corresonding to given product id
        if (product.getStockQuantity() == 0) {
            log.warn("Insufficient stock for productId={}", id);
            throw new InsufficientStockException("Insufficient stock for productId=" + id);
        }

        log.info("Product available with id={},Name={},Quantity={},Description={}", product.getId(), product.getProductName(), product.getStockQuantity(), product.getDescription());
        return AvailabilityResponseDto.builder()
                .productId(product.getId())
                .requestedQuantity(quantity)
                .available(product.getStockQuantity() >= quantity ? ProductAvailability.AVAILABLE : ProductAvailability.NOTAVAILABLE)
                .currentStock(product.getStockQuantity())
                .build();
    }

    @Override
    @Transactional
    public ProductResponseDto reduceStock(UUID id, Integer quantity, String orderReference) {
        Product product = productRepository.findById(id).orElseThrow(() ->
        {
            log.warn("The Product with ID: " + id + " does not exists, kindly enter valid ID.");
            throw new ProductNotFoundException("The Product with ID: " + id + " does not exists, kindly enter valid ID.");
        });
        // check for available quantity is sufficient for the order.
        if (product.getStockQuantity() < quantity) {
            log.warn("Insufficient stock for productId={} orderReference={} requested={} available={}",
                    id, orderReference, quantity, product.getStockQuantity());
            throw new InsufficientStockException(
                    "Insufficient stock for product " + id + ": requested " + quantity + ", available " + product.getStockQuantity());
        }
        // updating inventory with the remaining stock.
        product.setStockQuantity(product.getStockQuantity() - quantity);
        Product saved = productRepository.save(product);
        log.info("Stock reduced for productId={} by quantity={} due to orderReference={}", id, quantity, orderReference);
        productEventPublisher.publish(ProductEventType.PRODUCT_STOCK_CHANGED, saved);
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public String deleteProduct(UUID productId) {
        Product product = findProductOrThrow(productId);
        if (orderServiceClient.hasOpenOrders(productId)) {
            throw new ProductHasOpenOrdersException(
                    "Product " + productId + " cannot be deleted: it is referenced by one or more open orders");
        }
        product.setStatus(ProductStatus.DISCONTINUED);
        Product saved = productRepository.save(product);
        productEventPublisher.publish(ProductEventType.PRODUCT_DELETED, saved);

        return product.getProductName() + " was deleted";
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

    @Override
    @Transactional
    public ProductResponseDto updateProductInfo(UUID id, ProductRequestDto product) {
        Product savedProduct = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        productMapper.updateEntity(savedProduct, product);

        Product saved = productRepository.save(savedProduct);
        productEventPublisher.publish(ProductEventType.PRODUCT_UPDATED, saved);
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ProductResponseDto adjustStock(UUID id, StockUpdateRequestDto stockUpdateRequestDto) {
        Product product = findProductOrThrow(id);
        int current = product.getStockQuantity();
        int updated = switch (stockUpdateRequestDto.getOperation()) {
            case INCREASE -> current + stockUpdateRequestDto.getQuantity();
            case DECREASE -> current - stockUpdateRequestDto.getQuantity();
            case SET -> stockUpdateRequestDto.getQuantity();
        };
        if (updated < 0) {
            throw new InsufficientStockException(
                    "Cannot decrease stock of product " + id + " by " + stockUpdateRequestDto.getQuantity() + "; current stock is " + current);
        }
        product.setStockQuantity(updated);
        Product saved = productRepository.save(product);
        productEventPublisher.publish(ProductEventType.PRODUCT_STOCK_CHANGED, saved);
        return productMapper.toResponseDto(saved);
    }
}
