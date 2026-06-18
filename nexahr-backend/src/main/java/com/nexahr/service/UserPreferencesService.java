package com.nexahr.service;

import com.nexahr.dto.request.NotificationPreferencesRequest;
import com.nexahr.dto.response.NotificationPreferencesResponse;

public interface UserPreferencesService {
    NotificationPreferencesResponse getPreferences(Long userId);
    NotificationPreferencesResponse updatePreferences(Long userId, NotificationPreferencesRequest request);
}
