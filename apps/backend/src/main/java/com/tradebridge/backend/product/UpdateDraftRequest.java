package com.tradebridge.backend.product;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public record UpdateDraftRequest(@NotNull Map<String, String> parsedFields) {
}
