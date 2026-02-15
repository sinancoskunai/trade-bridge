package com.tradebridge.backend.category;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryBootstrap {

    private final CategoryService categoryService;

    public CategoryBootstrap(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostConstruct
    public void seedDefaultCategory() {
        categoryService.ensureDefaultCategory();
    }
}
