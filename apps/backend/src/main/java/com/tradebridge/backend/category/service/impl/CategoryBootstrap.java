package com.tradebridge.backend.category;

import org.springframework.stereotype.Component;

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
