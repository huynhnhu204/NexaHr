package com.nexahr.service.impl;

import com.nexahr.dto.response.NotificationResponse;
import com.nexahr.dto.response.PageResponse;
import com.nexahr.entity.Notification;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.NotificationRepository;
import com.nexahr.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl {

    private final NotificationRepository notificationRepository;

    public PageResponse<NotificationResponse> getByUser(Long userId, Pageable pageable) {
        return PageUtil.toPageResponse(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse));
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationRepository.save(n);
                });
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
