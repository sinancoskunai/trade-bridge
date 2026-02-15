package com.tradebridge.backend.search;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public record SearchQaRequest(@NotBlank String queryText, String categoryId, Map<String, String> contextFilters) {
}
