package com.nexahr.service;

import com.nexahr.entity.Notification;
import com.nexahr.entity.User;
import com.nexahr.entity.enums.NotificationType;

public interface NotificationDispatchService {
    Notification notify(User user, String title, String message, NotificationType type);
}
