package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferencesResponse {
    private boolean notifyEmailLeave;
    private boolean notifyEmailPayroll;
    private boolean notifyEmailSystem;
}
