package com.training.productservice.client;

import lombok.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;


@FeignClient(name = "order-service",url="${order-service.base-url}")
public interface OrderServiceClient {

    @GetMapping("/api/v1/orders/product/{productId}/has-open")
    boolean hasOpenOrders(@PathVariable("productId") UUID productId);
}
