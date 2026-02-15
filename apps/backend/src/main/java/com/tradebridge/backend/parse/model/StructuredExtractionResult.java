package com.tradebridge.backend.parse.model;

import java.util.Map;

public record StructuredExtractionResult(
        Map<String, String> fields,
        Map<String, Double> confidence,
        boolean usedModel,
        String modelName) {
}
