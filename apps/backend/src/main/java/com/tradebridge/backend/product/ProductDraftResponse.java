package com.tradebridge.backend.product;

import java.util.Map;

public record ProductDraftResponse(
        String draftId,
        String categoryId,
        String sellerUserId,
        String sourceFileName,
        Map<String, String> parsedFields,
        Map<String, Double> confidence,
        String status) {
}
