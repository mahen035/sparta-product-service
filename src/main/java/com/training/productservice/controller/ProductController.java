package com.training.productservice.controller;

import com.training.productservice.dto.AvailabilityResponseDto;
import com.training.productservice.dto.PriceUpdateRequestDto;
import com.training.productservice.dto.ProductRequestDto;
import com.training.productservice.dto.ProductResponseDto;
import com.training.productservice.dto.StockReductionRequestDto;
import com.training.productservice.dto.StockUpdateRequestDto;
import com.training.productservice.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto dto,
                                                              UriComponentsBuilder uriBuilder) {
        ProductResponseDto created = productService.createProduct(dto);
        URI location = uriBuilder.path("/api/v1/products/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable UUID id,
                                                              @Valid @RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductResponseDto> updatePrice(@PathVariable UUID id,
                                                            @Valid @RequestBody PriceUpdateRequestDto dto) {
        return ResponseEntity.ok(productService.updatePrice(id, dto));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponseDto> updateStock(@PathVariable UUID id,
                                                            @Valid @RequestBody StockUpdateRequestDto dto) {
        return ResponseEntity.ok(productService.updateStock(id, dto));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(@PathVariable UUID id,
                                                                       @RequestParam @Min(1) Integer quantity) {
        return ResponseEntity.ok(productService.checkAvailability(id, quantity));
    }

    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<ProductResponseDto> reduceStock(@PathVariable UUID id,
                                                            @Valid @RequestBody StockReductionRequestDto dto) {
        return ResponseEntity.ok(productService.reduceStock(id, dto.getQuantity(), dto.getOrderReference()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
