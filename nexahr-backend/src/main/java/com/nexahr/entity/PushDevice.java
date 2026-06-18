package com.nexahr.entity;

import com.nexahr.entity.enums.PushPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_devices", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_token"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", nullable = false)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PushPlatform platform;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
