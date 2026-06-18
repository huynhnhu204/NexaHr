package com.nexahr.service.impl;

import com.nexahr.dto.request.PushRegisterRequest;
import com.nexahr.dto.response.PushConfigResponse;
import com.nexahr.entity.PushDevice;
import com.nexahr.entity.User;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.PushDeviceRepository;
import com.nexahr.repository.UserRepository;
import com.nexahr.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final PushDeviceRepository pushDeviceRepository;
    private final UserRepository userRepository;

    @Value("${push.enabled:true}")
    private boolean pushEnabled;

    @Value("${push.fcm.server-key:}")
    private String fcmServerKey;

    @Override
    public PushConfigResponse getConfig() {
        boolean fcmReady = fcmServerKey != null && !fcmServerKey.isBlank();
        return PushConfigResponse.builder()
                .pushEnabled(pushEnabled)
                .provider(fcmReady ? "FCM" : "DEMO")
                .build();
    }

    @Override
    @Transactional
    public void registerDevice(Long userId, PushRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PushDevice device = pushDeviceRepository.findByUserIdAndDeviceToken(userId, request.getDeviceToken())
                .orElse(PushDevice.builder()
                        .user(user)
                        .deviceToken(request.getDeviceToken())
                        .platform(request.getPlatform())
                        .active(true)
                        .build());

        device.setPlatform(request.getPlatform());
        device.setActive(true);
        device.setLastUsedAt(LocalDateTime.now());
        pushDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public void unregisterDevice(Long userId, String deviceToken) {
        pushDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken)
                .ifPresent(device -> {
                    device.setActive(false);
                    pushDeviceRepository.save(device);
                });
    }

    @Override
    public void sendToUser(Long userId, String title, String message) {
        if (!pushEnabled) {
            return;
        }

        List<PushDevice> devices = pushDeviceRepository.findByUserIdAndActiveTrue(userId);
        if (devices.isEmpty()) {
            return;
        }

        boolean fcmReady = fcmServerKey != null && !fcmServerKey.isBlank();
        for (PushDevice device : devices) {
            if (fcmReady) {
                sendFcm(device.getDeviceToken(), title, message);
            } else {
                log.info("[PUSH-DEMO] user={} platform={} title={} message={}",
                        userId, device.getPlatform(), title, message);
            }
        }
    }

    private void sendFcm(String token, String title, String message) {
        log.info("[PUSH-FCM] token={} title={} (FCM key configured — integrate HTTP v1 in production)", token, title);
    }
}
