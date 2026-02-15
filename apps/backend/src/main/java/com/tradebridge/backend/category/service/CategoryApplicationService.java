package com.tradebridge.backend.category;

import java.util.List;

public interface CategoryApplicationService {

    List<CategoryResponse> list();

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse addAttribute(String categoryId, CategoryAttributeDefinition attribute);

    CategoryResponse getById(String id);

    void ensureDefaultCategory();
}
