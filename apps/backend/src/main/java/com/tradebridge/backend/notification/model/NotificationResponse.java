package com.tradebridge.backend.notification.model;

public record NotificationResponse(String id, String userId, String type, String message, long createdAtEpochMs) {
}
