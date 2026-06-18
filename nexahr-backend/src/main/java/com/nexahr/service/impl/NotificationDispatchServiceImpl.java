package com.nexahr.service.impl;

import com.nexahr.entity.Notification;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.NotificationType;
import com.nexahr.repository.NotificationRepository;
import com.nexahr.service.NotificationDispatchService;
import com.nexahr.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional
    public Notification notify(User user, String title, String message, NotificationType type) {
        if (user == null) {
            return null;
        }

        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build());

        pushNotificationService.sendToUser(user.getId(), title, message);
        return notification;
    }
}
