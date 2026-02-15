package com.tradebridge.backend.notification;

import java.util.List;

public interface NotificationApplicationService {

    void push(String userId, String type, String message);

    List<NotificationResponse> list(String userId);

    void registerDeviceToken(String userId, String token);
}
