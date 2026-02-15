package com.tradebridge.backend.product.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.product.persistence.entity.DocumentEntity;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
    Optional<DocumentEntity> findByDraftId(String draftId);
}
