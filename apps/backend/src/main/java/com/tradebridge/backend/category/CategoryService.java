package com.tradebridge.backend.category;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebridge.backend.category.persistence.CategoryAttributeEntity;
import com.tradebridge.backend.category.persistence.CategoryAttributeRepository;
import com.tradebridge.backend.category.persistence.CategoryEntity;
import com.tradebridge.backend.category.persistence.CategoryRepository;
import com.tradebridge.backend.common.ApiException;

@Service
public class CategoryService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final CategoryRepository categoryRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final ObjectMapper objectMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryAttributeRepository categoryAttributeRepository,
            ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        List<CategoryResponse> out = new ArrayList<>();
        for (CategoryEntity entity : categoryRepository.findAll()) {
            out.add(toResponse(entity));
        }
        return out;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        categoryRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Category already exists");
        });

        CategoryEntity category = new CategoryEntity();
        category.setId(UUID.randomUUID().toString());
        category.setName(request.name());
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());

        categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse addAttribute(String categoryId, CategoryAttributeDefinition attribute) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));

        CategoryAttributeEntity entity = new CategoryAttributeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setCategory(category);
        entity.setAttrKey(attribute.key());
        entity.setAttrType(attribute.type());
        entity.setRequired(attribute.required());
        entity.setEnumValuesJson(toJson(attribute.enumValues()));
        entity.setUnit(attribute.unit());
        entity.setFilterable(attribute.filterable());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        categoryAttributeRepository.save(entity);
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(String id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        return toResponse(category);
    }

    @Transactional
    public void ensureDefaultCategory() {
        if (categoryRepository.findByNameIgnoreCase("Kuruyemis").isPresent()) {
            return;
        }

        CategoryEntity category = new CategoryEntity();
        category.setId(UUID.randomUUID().toString());
        category.setName("Kuruyemis");
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        categoryRepository.save(category);

        addDefaultAttribute(category, "urun_adi", "STRING", true, null, true);
        addDefaultAttribute(category, "agirlik_kg", "NUMBER", false, "kg", true);
    }

    private CategoryResponse toResponse(CategoryEntity category) {
        List<CategoryAttributeDefinition> attributes = categoryAttributeRepository
                .findByCategoryIdOrderByAttrKeyAsc(category.getId())
                .stream()
                .map(this::toAttribute)
                .toList();
        return new CategoryResponse(category.getId(), category.getName(), attributes);
    }

    private void addDefaultAttribute(
            CategoryEntity category,
            String key,
            String type,
            boolean required,
            String unit,
            boolean filterable) {
        CategoryAttributeEntity entity = new CategoryAttributeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setCategory(category);
        entity.setAttrKey(key);
        entity.setAttrType(type);
        entity.setRequired(required);
        entity.setEnumValuesJson(null);
        entity.setUnit(unit);
        entity.setFilterable(filterable);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        categoryAttributeRepository.save(entity);
    }

    private CategoryAttributeDefinition toAttribute(CategoryAttributeEntity entity) {
        return new CategoryAttributeDefinition(
                entity.getAttrKey(),
                entity.getAttrType(),
                entity.isRequired(),
                fromJson(entity.getEnumValuesJson()),
                entity.getUnit(),
                entity.isFilterable());
    }

    private String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid enum values");
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
