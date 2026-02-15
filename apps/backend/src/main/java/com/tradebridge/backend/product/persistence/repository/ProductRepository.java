package com.tradebridge.backend.product.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.product.persistence.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    List<ProductEntity> findByActiveTrue();

    List<ProductEntity> findByCategoryIdAndActiveTrue(String categoryId);
}
