package com.tradebridge.backend.category;

import java.util.List;

public record CategoryResponse(String id, String name, List<CategoryAttributeDefinition> attributes) {
}
