package com.tradebridge.backend.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDraftRepository extends JpaRepository<ProductDraftEntity, String> {
}
