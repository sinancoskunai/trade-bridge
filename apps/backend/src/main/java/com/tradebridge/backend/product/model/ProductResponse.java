package com.tradebridge.backend.product.model;

import java.util.Map;

public record ProductResponse(String productId, String categoryId, String sellerCompanyId, Map<String, String> attributes,
        boolean active) {
}
