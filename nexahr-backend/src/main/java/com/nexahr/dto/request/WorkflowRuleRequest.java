package com.nexahr.dto.request;

import com.nexahr.entity.enums.WorkflowAction;
import com.nexahr.entity.enums.WorkflowTrigger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowRuleRequest {
    @NotBlank
    private String name;

    @NotNull
    private WorkflowTrigger trigger;

    @NotNull
    private WorkflowAction action;

    private String configValue;

    private Boolean active;
}
