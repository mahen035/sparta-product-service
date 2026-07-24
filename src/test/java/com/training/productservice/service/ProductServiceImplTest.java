package com.training.productservice.service;

import com.training.productservice.dto.AvailabilityResponseDto;
import com.training.productservice.enums.ProductAvailability;
import com.training.productservice.entity.Product;
import com.training.productservice.enums.ProductStatus;
import com.training.productservice.event.ProductEventPublisher;
import com.training.productservice.exception.InsufficientStockException;
import com.training.productservice.exception.ProductNotFoundException;
import com.training.productservice.repository.ProductRepository;
import com.training.productservice.util.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.training.productservice.dto.ProductResponseDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private ProductServiceImpl productServiceImpl;

    private Product buildProduct(UUID id, int stock) {
        return Product.builder()
                .id(id)
                .productName("Test Product")
                .category("Electronics")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(stock)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    // Positive: stock is sufficient for requested quantity
    @Test
    void checkAvailability_whenStockSufficient_returnsAvailable() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 10);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        AvailabilityResponseDto result = productServiceImpl.checkAvailability(id, 5);

        assertEquals(id, result.getProductId());
        assertEquals(5, result.getRequestedQuantity());
        assertEquals(10, result.getCurrentStock());
        assertEquals(ProductAvailability.AVAILABLE, result.getAvailable());
        verify(productRepository, times(1)).findById(id);
    }

    // Positive: stock exactly equals requested quantity — boundary case
    @Test
    void checkAvailability_whenStockEqualsRequestedQuantity_returnsAvailable() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 5);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        AvailabilityResponseDto result = productServiceImpl.checkAvailability(id, 5);

        assertEquals(ProductAvailability.AVAILABLE, result.getAvailable());
        assertEquals(5, result.getCurrentStock());
        assertEquals(5, result.getRequestedQuantity());
        verify(productRepository, times(1)).findById(id);
    }

    // Positive: stock is less than requested quantity — returns NOTAVAILABLE (not an exception)
    @Test
    void checkAvailability_whenStockLessThanRequestedQuantity_returnsNotAvailable() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 3);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        AvailabilityResponseDto result = productServiceImpl.checkAvailability(id, 10);

        assertEquals(id, result.getProductId());
        assertEquals(10, result.getRequestedQuantity());
        assertEquals(3, result.getCurrentStock());
        assertEquals(ProductAvailability.NOTAVAILABLE, result.getAvailable());
        verify(productRepository, times(1)).findById(id);
    }

    // Negative: product ID does not exist in repository
    @Test
    void checkAvailability_whenProductNotFound_throwsProductNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> productServiceImpl.checkAvailability(id, 5));

        assertTrue(ex.getMessage().contains(id.toString()));
        verify(productRepository, times(1)).findById(id);
    }

    // Negative: product exists but stock is zero
    @Test
    void checkAvailability_whenStockIsZero_throwsInsufficientStockException() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 0);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> productServiceImpl.checkAvailability(id, 5));

        assertTrue(ex.getMessage().contains(id.toString()));
        verify(productRepository, times(1)).findById(id);
    }

    // -------------------------
    // reduceStock test cases
    // -------------------------

    // Positive: stock is greater than requested quantity — stock is reduced and response is returned
    @Test
    void reduceStock_whenStockSufficient_reducesStockAndReturnsResponse() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 10);
        Product saved = buildProduct(id, 7);
        ProductResponseDto expectedDto = new ProductResponseDto();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(saved);
        when(productMapper.toResponseDto(saved)).thenReturn(expectedDto);

        ProductResponseDto result = productServiceImpl.reduceStock(id, 3, "ORD-001");

        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertEquals(7, product.getStockQuantity());
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(product);
        verify(productMapper, times(1)).toResponseDto(saved);
    }

    // Positive: stock exactly equals requested quantity — boundary case, stock becomes 0
    @Test
    void reduceStock_whenStockEqualsRequestedQuantity_reducesStockToZero() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 5);
        Product saved = buildProduct(id, 0);
        ProductResponseDto expectedDto = new ProductResponseDto();
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(saved);
        when(productMapper.toResponseDto(saved)).thenReturn(expectedDto);

        ProductResponseDto result = productServiceImpl.reduceStock(id, 5, "ORD-002");

        assertNotNull(result);
        assertEquals(0, product.getStockQuantity());
        verify(productRepository, times(1)).save(product);
    }

    // Negative: product ID does not exist in repository
    @Test
    void reduceStock_whenProductNotFound_throwsProductNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> productServiceImpl.reduceStock(id, 3, "ORD-003"));

        assertTrue(ex.getMessage().contains(id.toString()));
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).save(any());
    }

    // Negative: requested quantity exceeds available stock
    @Test
    void reduceStock_whenRequestedQuantityExceedsStock_throwsInsufficientStockException() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, 2);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> productServiceImpl.reduceStock(id, 10, "ORD-004"));

        assertTrue(ex.getMessage().contains(id.toString()));
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, never()).save(any());
    }
}
