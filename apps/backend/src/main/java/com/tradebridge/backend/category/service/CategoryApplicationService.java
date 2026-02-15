package com.tradebridge.backend.category.service;

import java.util.List;

import com.tradebridge.backend.category.model.CategoryAttributeDefinition;
import com.tradebridge.backend.category.model.CategoryResponse;
import com.tradebridge.backend.category.model.CreateCategoryRequest;

public interface CategoryApplicationService {

    List<CategoryResponse> list();

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse addAttribute(String categoryId, CategoryAttributeDefinition attribute);

    CategoryResponse getById(String id);

    void ensureDefaultCategory();
}
