package com.nexahr.entity;

import com.nexahr.entity.enums.PermissionCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_role_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"custom_role_id", "permission"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomRolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_role_id", nullable = false)
    private CustomRole customRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionCode permission;

    @Column(nullable = false)
    @Builder.Default
    private boolean granted = true;
}
