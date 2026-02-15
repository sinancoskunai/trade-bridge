package com.tradebridge.backend.product.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.product.persistence.entity.ParseJobEntity;

public interface ParseJobRepository extends JpaRepository<ParseJobEntity, String> {
    List<ParseJobEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<ParseJobEntity> findAllByOrderByCreatedAtDesc();
}
