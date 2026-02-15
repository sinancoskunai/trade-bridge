package com.tradebridge.backend.category.service.impl;

import org.springframework.stereotype.Component;

import com.tradebridge.backend.category.service.CategoryApplicationService;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryBootstrap {

    private final CategoryApplicationService categoryService;

    public CategoryBootstrap(CategoryApplicationService categoryService) {
        this.categoryService = categoryService;
    }

    @PostConstruct
    public void seedDefaultCategory() {
        categoryService.ensureDefaultCategory();
    }
}
