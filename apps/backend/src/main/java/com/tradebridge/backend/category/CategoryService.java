package com.tradebridge.backend.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tradebridge.backend.common.ApiException;

@Service
public class CategoryService {

    private final Map<String, CategoryMutable> categories = new ConcurrentHashMap<>();

    public CategoryService() {
        String id = UUID.randomUUID().toString();
        CategoryMutable seed = new CategoryMutable(id, "Kuruyemis");
        seed.attributes.add(new CategoryAttributeDefinition("urun_adi", "STRING", true, null, null, true));
        seed.attributes.add(new CategoryAttributeDefinition("agirlik_kg", "NUMBER", false, null, "kg", true));
        categories.put(id, seed);
    }

    public List<CategoryResponse> list() {
        return categories.values().stream().map(CategoryMutable::toResponse).toList();
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        String id = UUID.randomUUID().toString();
        CategoryMutable category = new CategoryMutable(id, request.name());
        categories.put(id, category);
        return category.toResponse();
    }

    public CategoryResponse addAttribute(String categoryId, CategoryAttributeDefinition attribute) {
        CategoryMutable category = categories.get(categoryId);
        if (category == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found");
        }
        category.attributes.add(attribute);
        return category.toResponse();
    }

    public CategoryResponse getById(String id) {
        CategoryMutable category = categories.get(id);
        if (category == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found");
        }
        return category.toResponse();
    }

    private static final class CategoryMutable {
        private final String id;
        private final String name;
        private final List<CategoryAttributeDefinition> attributes = new ArrayList<>();

        private CategoryMutable(String id, String name) {
            this.id = id;
            this.name = name;
        }

        CategoryResponse toResponse() {
            return new CategoryResponse(id, name, List.copyOf(attributes));
        }
    }
}
