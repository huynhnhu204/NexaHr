package com.nexahr.dto.response;

import com.nexahr.entity.enums.WorkflowAction;
import com.nexahr.entity.enums.WorkflowTrigger;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowRuleResponse {
    private Long id;
    private String name;
    private WorkflowTrigger trigger;
    private WorkflowAction action;
    private String configValue;
    private boolean active;
    private LocalDateTime createdAt;
}
