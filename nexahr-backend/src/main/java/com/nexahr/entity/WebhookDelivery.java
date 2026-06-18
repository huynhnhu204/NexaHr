package com.nexahr.entity;

import com.nexahr.entity.enums.WebhookEvent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", nullable = false)
    private WebhookEndpoint webhookEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookEvent event;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    @Builder.Default
    private boolean success = false;

    @CreationTimestamp
    @Column(name = "attempted_at", updatable = false)
    private LocalDateTime attemptedAt;
}
