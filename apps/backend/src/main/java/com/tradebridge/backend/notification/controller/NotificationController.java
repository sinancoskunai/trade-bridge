package com.tradebridge.backend.notification;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradebridge.backend.auth.SecurityUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationApplicationService notificationService;

    public NotificationController(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list() {
        return notificationService.list(SecurityUtil.currentUser().userId());
    }

    @PostMapping("/device-token")
    public void registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        notificationService.registerDeviceToken(SecurityUtil.currentUser().userId(), request.token());
    }
}
