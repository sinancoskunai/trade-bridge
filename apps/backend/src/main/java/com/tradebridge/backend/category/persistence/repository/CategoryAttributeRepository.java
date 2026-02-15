package com.tradebridge.backend.category.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradebridge.backend.category.persistence.entity.CategoryAttributeEntity;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttributeEntity, String> {
    List<CategoryAttributeEntity> findByCategoryIdOrderByAttrKeyAsc(String categoryId);
}
