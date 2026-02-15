package com.tradebridge.backend.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    List<ProductEntity> findByActiveTrue();

    List<ProductEntity> findByCategoryIdAndActiveTrue(String categoryId);
}
