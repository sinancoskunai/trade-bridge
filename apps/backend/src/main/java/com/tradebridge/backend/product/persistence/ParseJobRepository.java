package com.tradebridge.backend.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParseJobRepository extends JpaRepository<ParseJobEntity, String> {
    List<ParseJobEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<ParseJobEntity> findAllByOrderByCreatedAtDesc();
}
