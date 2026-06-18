package com.nexahr.service;

import com.nexahr.dto.request.PushRegisterRequest;
import com.nexahr.dto.response.PushConfigResponse;

public interface PushNotificationService {
    void registerDevice(Long userId, PushRegisterRequest request);
    void unregisterDevice(Long userId, String deviceToken);
    void sendToUser(Long userId, String title, String message);
    PushConfigResponse getConfig();
}
