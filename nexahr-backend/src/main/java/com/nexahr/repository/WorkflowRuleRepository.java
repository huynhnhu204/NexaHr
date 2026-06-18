package com.nexahr.repository;

import com.nexahr.entity.WorkflowRule;
import com.nexahr.entity.enums.WorkflowTrigger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowRuleRepository extends JpaRepository<WorkflowRule, Long> {
    List<WorkflowRule> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<WorkflowRule> findByCompanyIdAndTriggerAndActiveTrue(Long companyId, WorkflowTrigger trigger);
}
