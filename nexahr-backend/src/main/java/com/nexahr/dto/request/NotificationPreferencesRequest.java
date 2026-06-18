package com.nexahr.dto.request;

import lombok.Data;

@Data
public class NotificationPreferencesRequest {
    private Boolean notifyEmailLeave;
    private Boolean notifyEmailPayroll;
    private Boolean notifyEmailSystem;
}
