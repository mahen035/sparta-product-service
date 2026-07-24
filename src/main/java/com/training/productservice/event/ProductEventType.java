package com.training.productservice.event;

/**
 * Discriminator for messages published to the {@code product-events} Kafka topic.
 */
public enum ProductEventType {
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_PRICE_CHANGED,
    PRODUCT_STOCK_CHANGED,
    PRODUCT_DELETED
}
