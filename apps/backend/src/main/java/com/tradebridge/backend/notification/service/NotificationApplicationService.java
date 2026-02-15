package com.tradebridge.backend.notification.service;

import java.util.List;

import com.tradebridge.backend.notification.model.NotificationResponse;

public interface NotificationApplicationService {

    void push(String userId, String type, String message);

    List<NotificationResponse> list(String userId);

    void registerDeviceToken(String userId, String token);
}
