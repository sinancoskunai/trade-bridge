package com.tradebridge.backend.product.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.product.persistence.entity.ProductDraftEntity;

public interface ProductDraftRepository extends JpaRepository<ProductDraftEntity, String> {
}
