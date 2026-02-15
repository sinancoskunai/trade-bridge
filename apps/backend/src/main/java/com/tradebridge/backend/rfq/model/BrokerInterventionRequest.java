package com.tradebridge.backend.rfq.model;

import jakarta.validation.constraints.NotBlank;

public record BrokerInterventionRequest(@NotBlank String note) {
}
