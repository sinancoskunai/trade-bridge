package com.tradebridge.backend.category.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.category.persistence.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
