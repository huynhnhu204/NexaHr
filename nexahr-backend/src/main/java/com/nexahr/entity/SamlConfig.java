package com.nexahr.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saml_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamlConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "idp_name")
    private String idpName;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "sso_url")
    private String ssoUrl;

    @Column(columnDefinition = "TEXT")
    private String certificate;

    @Column(name = "attribute_email")
    @Builder.Default
    private String attributeEmail = "email";
}
