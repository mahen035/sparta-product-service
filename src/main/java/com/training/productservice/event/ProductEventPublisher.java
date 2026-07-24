package com.training.productservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import com.training.productservice.entity.Product;

/**
 * Publishes {@link ProductEvent}s to the {@code product-events} topic. Fire-and-forget:
 * a publish failure must never fail the product CRUD/stock operation that triggered it.
 */
@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${app.kafka.topic.product-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(ProductEventType eventType, Product product) {
        ProductEvent event = new ProductEvent(
                UUID.randomUUID(),
                eventType,
                product.getId(),
                product.getProductName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                LocalDateTime.now());

        try {
            kafkaTemplate.send(topic, event.productId().toString(), event).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish {} for product {}: {}", eventType, product.getId(), ex.getMessage(), ex);
                } else {
                    log.info("Published {} for product {} [partition={}, offset={}]", eventType, product.getId(),
                            result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
        } catch (Exception ex) {
            // KafkaTemplate.send() can itself throw synchronously (e.g. a metadata-fetch
            // timeout when the broker is unreachable) rather than failing the returned
            // future - never let that take down the product create/update/stock request.
            log.error("Failed to publish {} for product {}: {}", eventType, product.getId(), ex.getMessage(), ex);
        }
    }
}
