package com.tradebridge.backend.rfq;

import jakarta.validation.constraints.NotBlank;

public record RfqRequest(@NotBlank String categoryId, @NotBlank String requirementText) {
}
