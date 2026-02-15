package com.tradebridge.backend.notification;

public record NotificationResponse(String id, String userId, String type, String message, long createdAtEpochMs) {
}
