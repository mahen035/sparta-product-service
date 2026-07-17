package com.training.productservice.controller;

import com.training.productservice.dto.AvailabilityResponseDto;
import com.training.productservice.dto.PriceUpdateRequestDto;
import com.training.productservice.dto.ProductRequestDto;
import com.training.productservice.dto.ProductResponseDto;
import com.training.productservice.dto.StockReductionRequestDto;
import com.training.productservice.dto.StockUpdateRequestDto;
import com.training.productservice.dto.ErrorResponseDto;
import com.training.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Products", description = "Product catalog, pricing, and stock management")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
})
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created",
            content = @Content(schema = @Schema(implementation = ProductResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "A product with the same name and category already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto dto,
                                                              UriComponentsBuilder uriBuilder) {
        ProductResponseDto created = productService.createProduct(dto);
        URI location = uriBuilder.path("/api/v1/products/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List all active products")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ResponseEntity<ProductResponseDto> getProductById(@Parameter(description = "Product id") @PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Update a product's price")
    public ResponseEntity<ProductResponseDto> updatePrice(@Parameter(description = "Product id") @PathVariable UUID id,
                                                            @Valid @RequestBody PriceUpdateRequestDto dto) {
        return ResponseEntity.ok(productService.updatePrice(id, dto));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check whether a product has sufficient stock for a requested quantity")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(@Parameter(description = "Product id") @PathVariable UUID id,
                                                                       @Parameter(description = "Requested quantity")
                                                                       @RequestParam @NotNull @Min(1) Integer quantity) {
        return ResponseEntity.ok(productService.checkAvailability(id, quantity));
    }

    @PatchMapping("/{id}/reduce-stock")
    @Operation(summary = "Reduce stock for an order")
    @ApiResponse(responseCode = "409", description = "Insufficient stock for the requested quantity",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ProductResponseDto> reduceStock(@Parameter(description = "Product id") @PathVariable UUID id,
                                                            @Valid @RequestBody StockReductionRequestDto dto) {
        return ResponseEntity.ok(productService.reduceStock(id, dto.getQuantity(), dto.getOrderReference()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a product")
    @ApiResponse(responseCode = "204", description = "Product deactivated")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product id") @PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update product info (name, description, category, price, stock)")
    public ResponseEntity<ProductResponseDto> updateProductInfo(@Parameter(description = "Product id") @PathVariable UUID id,
                                                                  @Valid @RequestBody ProductRequestDto product) {
        ProductResponseDto updatedProduct = productService.updateProductInfo(id, product);
        return ResponseEntity.status(HttpStatus.OK)
                .header("X-Update", "resource updated successfully")
                .body(updatedProduct);
    }

    @PatchMapping("/adjust/stock/{id}")
    @Operation(summary = "Adjust stock by increasing, decreasing, or setting the quantity")
    @ApiResponse(responseCode = "409", description = "Resulting stock would be negative",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ProductResponseDto> adjustStock(@Parameter(description = "Product id") @PathVariable UUID id,
                                                             @Valid @RequestBody StockUpdateRequestDto stockUpdateRequestDto) {
        ProductResponseDto updatedStock = productService.adjustStock(id, stockUpdateRequestDto);
        return ResponseEntity.status(HttpStatus.OK)
                .header("X-Stock-Update", "stock value updated--> " + stockUpdateRequestDto.getOperation())
                .body(updatedStock);
    }
}
