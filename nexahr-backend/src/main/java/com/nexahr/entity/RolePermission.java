package com.nexahr.entity;

import com.nexahr.entity.enums.PermissionCode;
import com.nexahr.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "role", "permission"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionCode permission;

    @Column(nullable = false)
    @Builder.Default
    private boolean granted = true;
}
