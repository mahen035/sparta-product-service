package com.training.productservice.repository;

import com.training.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByProductNameIgnoreCaseAndCategoryIgnoreCase(String productName, String category);

}
