package com.tradebridge.backend.rfq;

import jakarta.validation.constraints.NotBlank;

public record BrokerInterventionRequest(@NotBlank String note) {
}
