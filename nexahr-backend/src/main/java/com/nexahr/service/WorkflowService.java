package com.nexahr.service;

import com.nexahr.dto.request.WorkflowRuleRequest;
import com.nexahr.dto.response.WorkflowRuleResponse;
import com.nexahr.entity.LeaveRequest;

import java.util.List;

public interface WorkflowService {
    List<WorkflowRuleResponse> list(Long companyId);
    WorkflowRuleResponse create(Long companyId, WorkflowRuleRequest request);
    WorkflowRuleResponse update(Long companyId, Long id, WorkflowRuleRequest request);
    void delete(Long companyId, Long id);
    void onLeaveCreated(LeaveRequest leave);
}
