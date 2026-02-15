package com.tradebridge.backend.category.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryAttributeDefinition(
        @NotBlank String key,
        @NotBlank String type,
        @NotNull Boolean required,
        List<String> enumValues,
        String unit,
        @NotNull Boolean filterable) {
}
