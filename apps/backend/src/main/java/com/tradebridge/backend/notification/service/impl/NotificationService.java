package com.tradebridge.backend.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class NotificationService implements NotificationApplicationService {

    private final Map<String, List<NotificationResponse>> byUser = new ConcurrentHashMap<>();
    private final Map<String, String> deviceTokens = new ConcurrentHashMap<>();

    @Override
    public void push(String userId, String type, String message) {
        NotificationResponse n = new NotificationResponse(
                UUID.randomUUID().toString(),
                userId,
                type,
                message,
                System.currentTimeMillis());
        byUser.computeIfAbsent(userId, unused -> new ArrayList<>()).add(0, n);
    }

    @Override
    public List<NotificationResponse> list(String userId) {
        return byUser.getOrDefault(userId, List.of());
    }

    @Override
    public void registerDeviceToken(String userId, String token) {
        deviceTokens.put(userId, token);
    }
}
