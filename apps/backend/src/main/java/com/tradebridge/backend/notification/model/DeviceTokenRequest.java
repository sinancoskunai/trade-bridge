package com.tradebridge.backend.notification;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(@NotBlank String token) {
}
