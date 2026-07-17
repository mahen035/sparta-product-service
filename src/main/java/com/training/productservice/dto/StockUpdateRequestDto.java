package com.training.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequestDto {

    @NotNull
    @Min(1)
    @Schema(description = "Amount to apply for the given operation", example = "10")
    private Integer quantity;

    @NotNull
    @Schema(description = "How quantity is applied: INCREASE/DECREASE adjust current stock, SET replaces it")
    private StockOperation operation;
}
