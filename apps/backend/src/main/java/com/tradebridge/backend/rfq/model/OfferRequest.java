package com.tradebridge.backend.rfq.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OfferRequest(@Positive double price, @NotBlank String currency, @NotBlank String note) {
}
