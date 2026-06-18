package com.nexahr.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchCompanyRequest {
    @NotNull(message = "companyId là bắt buộc")
    private Long companyId;
}
