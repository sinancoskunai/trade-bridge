package com.tradebridge.backend.rfq.model;

import jakarta.validation.constraints.NotBlank;

public record RfqRequest(@NotBlank String categoryId, @NotBlank String requirementText) {
}
