package com.tradebridge.backend.notification.model;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(@NotBlank String token) {
}
