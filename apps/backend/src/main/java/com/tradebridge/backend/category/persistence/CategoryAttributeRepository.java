package com.tradebridge.backend.category.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttributeEntity, String> {
    List<CategoryAttributeEntity> findByCategoryIdOrderByAttrKeyAsc(String categoryId);
}
