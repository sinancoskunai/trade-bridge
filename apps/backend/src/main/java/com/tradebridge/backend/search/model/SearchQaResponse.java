package com.tradebridge.backend.search.model;

import java.util.List;
import java.util.Map;

import com.tradebridge.backend.product.model.ProductResponse;

public record SearchQaResponse(
        Map<String, String> interpretedFilters,
        List<String> followUpQuestions,
        List<ProductResponse> matchedProducts,
        String explanation) {
}
