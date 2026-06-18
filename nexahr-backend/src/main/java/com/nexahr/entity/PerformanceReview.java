package com.nexahr.entity;

import com.nexahr.entity.enums.PerformanceRating;
import com.nexahr.entity.enums.PerformanceReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @Column(columnDefinition = "TEXT")
    private String goals;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PerformanceReviewStatus status = PerformanceReviewStatus.DRAFT;

    @Column(name = "employee_self_comment", columnDefinition = "TEXT")
    private String employeeSelfComment;

    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    private PerformanceRating rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
