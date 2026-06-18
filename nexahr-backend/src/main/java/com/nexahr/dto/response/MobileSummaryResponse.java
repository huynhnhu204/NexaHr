package com.nexahr.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileSummaryResponse {
    private String fullName;
    private boolean checkedInToday;
    private boolean checkedOutToday;
    private long pendingLeaves;
    private long unreadNotifications;
    private String todayAttendanceStatus;
    private String role;
    private String companyName;
}
