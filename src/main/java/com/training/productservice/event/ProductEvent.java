package com.training.productservice.event;

import com.training.productservice.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Envelope published to the {@code product-events} Kafka topic. Partition key is the
 * product id, so all events for a given product are ordered. Consumed by
 * notification-service to raise in-app notifications for the catalog/ops audience.
 */
public record ProductEvent(
        UUID eventId,
        ProductEventType eventType,
        UUID productId,
        String productName,
        String category,
        BigDecimal price,
        Integer stockQuantity,
        ProductStatus status,
        LocalDateTime occurredAt
) {
}
