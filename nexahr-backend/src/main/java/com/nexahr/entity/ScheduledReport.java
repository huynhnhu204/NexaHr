package com.nexahr.entity;

import com.nexahr.entity.enums.ReportFrequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(name = "report_type", nullable = false)
    @Builder.Default
    private String reportType = "WORKFORCE";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportFrequency frequency = ReportFrequency.WEEKLY;

    @Column(name = "recipient_emails", nullable = false)
    private String recipientEmails;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
