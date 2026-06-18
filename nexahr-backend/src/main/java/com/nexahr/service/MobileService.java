package com.nexahr.service;

import com.nexahr.dto.response.MobileSummaryResponse;

public interface MobileService {
    MobileSummaryResponse getSummary(Long userId);
}
