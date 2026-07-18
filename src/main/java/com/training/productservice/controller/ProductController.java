package com.training.productservice.controller;

import com.training.productservice.dto.*;
import com.training.productservice.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductResponseDto> updatePrice(@PathVariable UUID id,
                                                          @Valid @RequestBody PriceUpdateRequestDto dto) {
        return ResponseEntity.ok(productService.updatePrice(id, dto));
    }

    @GetMapping("/availability/{id}")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(@PathVariable @NotBlank UUID id,
                                                                     @RequestParam @NotNull @Min(1) Integer quantity) {
        return ResponseEntity.ok(productService.checkAvailability(id, quantity));
    }

    @PatchMapping("/reduce/stock/{id}")
    public ResponseEntity<ProductResponseDto> reduceStock(@PathVariable UUID id,
                                                          @Valid @RequestBody StockReductionRequestDto dto) {
        return ResponseEntity.ok(productService.reduceStock(id, dto.getQuantity(), dto.getOrderReference()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponseDto> updateProductInfo(@PathVariable UUID id, @Valid @RequestBody ProductRequestDto product) {
        ProductResponseDto updatedProduct = productService.updateProductInfo(id, product);

        return ResponseEntity.status(HttpStatus.OK)
                .header("update", "resource updated successfully...")
                .body(updatedProduct);
    }

    @PatchMapping("/adjust/stock/{id}")
    public ResponseEntity<ProductResponseDto> adjustStock(@PathVariable UUID id, @Valid @RequestBody @Min(1) StockUpdateRequestDto stockUpdateRequestDto) {
        ProductResponseDto updatedStock = productService.adjustStock(id, stockUpdateRequestDto);

        return ResponseEntity.status(HttpStatus.OK)
                .header("stock update", "stock value updated--> " + stockUpdateRequestDto.getOperation())
                .body(updatedStock);
    }
}
