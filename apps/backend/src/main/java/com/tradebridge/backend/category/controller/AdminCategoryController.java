package com.tradebridge.backend.category.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradebridge.backend.category.model.CategoryAttributeDefinition;
import com.tradebridge.backend.category.model.CategoryResponse;
import com.tradebridge.backend.category.model.CreateCategoryRequest;
import com.tradebridge.backend.category.service.CategoryApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryApplicationService categoryService;

    public AdminCategoryController(CategoryApplicationService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(request);
    }

    @PostMapping("/{id}/attributes")
    public CategoryResponse addAttribute(@PathVariable("id") String categoryId,
            @Valid @RequestBody CategoryAttributeDefinition attribute) {
        return categoryService.addAttribute(categoryId, attribute);
    }
}
