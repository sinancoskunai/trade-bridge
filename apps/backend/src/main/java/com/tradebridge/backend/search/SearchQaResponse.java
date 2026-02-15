package com.tradebridge.backend.search;

import java.util.List;
import java.util.Map;

import com.tradebridge.backend.product.ProductResponse;

public record SearchQaResponse(
        Map<String, String> interpretedFilters,
        List<String> followUpQuestions,
        List<ProductResponse> matchedProducts,
        String explanation) {
}
