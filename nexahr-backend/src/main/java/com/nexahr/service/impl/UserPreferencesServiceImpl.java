package com.nexahr.service.impl;

import com.nexahr.dto.request.NotificationPreferencesRequest;
import com.nexahr.dto.response.NotificationPreferencesResponse;
import com.nexahr.entity.User;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.UserRepository;
import com.nexahr.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferencesServiceImpl implements UserPreferencesService {

    private final UserRepository userRepository;

    @Override
    public NotificationPreferencesResponse getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public NotificationPreferencesResponse updatePreferences(Long userId, NotificationPreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (request.getNotifyEmailLeave() != null) {
            user.setNotifyEmailLeave(request.getNotifyEmailLeave());
        }
        if (request.getNotifyEmailPayroll() != null) {
            user.setNotifyEmailPayroll(request.getNotifyEmailPayroll());
        }
        if (request.getNotifyEmailSystem() != null) {
            user.setNotifyEmailSystem(request.getNotifyEmailSystem());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    private NotificationPreferencesResponse toResponse(User user) {
        return NotificationPreferencesResponse.builder()
                .notifyEmailLeave(user.isNotifyEmailLeave())
                .notifyEmailPayroll(user.isNotifyEmailPayroll())
                .notifyEmailSystem(user.isNotifyEmailSystem())
                .build();
    }
}
