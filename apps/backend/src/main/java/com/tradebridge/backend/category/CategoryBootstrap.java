package com.tradebridge.backend.category;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tradebridge.backend.category.persistence.CategoryAttributeEntity;
import com.tradebridge.backend.category.persistence.CategoryAttributeRepository;
import com.tradebridge.backend.category.persistence.CategoryEntity;
import com.tradebridge.backend.category.persistence.CategoryRepository;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryBootstrap {

    private final CategoryRepository categoryRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;

    public CategoryBootstrap(
            CategoryRepository categoryRepository,
            CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryAttributeRepository = categoryAttributeRepository;
    }

    @PostConstruct
    public void seedDefaultCategory() {
        if (categoryRepository.findByNameIgnoreCase("Kuruyemis").isPresent()) {
            return;
        }

        CategoryEntity category = new CategoryEntity();
        category.setId(UUID.randomUUID().toString());
        category.setName("Kuruyemis");
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());
        categoryRepository.save(category);

        CategoryAttributeEntity urunAdi = new CategoryAttributeEntity();
        urunAdi.setId(UUID.randomUUID().toString());
        urunAdi.setCategory(category);
        urunAdi.setAttrKey("urun_adi");
        urunAdi.setAttrType("STRING");
        urunAdi.setRequired(true);
        urunAdi.setEnumValuesJson(null);
        urunAdi.setUnit(null);
        urunAdi.setFilterable(true);
        urunAdi.setCreatedAt(Instant.now());
        urunAdi.setUpdatedAt(Instant.now());

        CategoryAttributeEntity agirlik = new CategoryAttributeEntity();
        agirlik.setId(UUID.randomUUID().toString());
        agirlik.setCategory(category);
        agirlik.setAttrKey("agirlik_kg");
        agirlik.setAttrType("NUMBER");
        agirlik.setRequired(false);
        agirlik.setEnumValuesJson(null);
        agirlik.setUnit("kg");
        agirlik.setFilterable(true);
        agirlik.setCreatedAt(Instant.now());
        agirlik.setUpdatedAt(Instant.now());

        categoryAttributeRepository.save(urunAdi);
        categoryAttributeRepository.save(agirlik);
    }
}
