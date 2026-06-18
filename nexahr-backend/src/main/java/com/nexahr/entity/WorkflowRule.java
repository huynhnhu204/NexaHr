package com.nexahr.entity;

import com.nexahr.entity.enums.WorkflowAction;
import com.nexahr.entity.enums.WorkflowTrigger;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_trigger", nullable = false)
    private WorkflowTrigger trigger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowAction action;

    @Column(name = "config_value")
    private String configValue;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
