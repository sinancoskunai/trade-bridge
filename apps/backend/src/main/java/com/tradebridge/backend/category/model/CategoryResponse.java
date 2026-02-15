package com.tradebridge.backend.category.model;

import java.util.List;

public record CategoryResponse(String id, String name, List<CategoryAttributeDefinition> attributes) {
}
