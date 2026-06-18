package com.nexahr.entity;

import com.nexahr.entity.enums.InterviewMode;
import com.nexahr.entity.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Employee interviewer;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InterviewMode mode = InterviewMode.ONLINE;

    private String location;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String evaluation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
