package com.tradebridge.backend.product.model;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public record UpdateDraftRequest(@NotNull Map<String, String> parsedFields) {
}
