package com.training.productservice.repository;

import com.training.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategory(String category);

    List<Product> findByActiveTrue();

    boolean existsByProductNameIgnoreCaseAndCategoryIgnoreCase(String productName, String category);
}
