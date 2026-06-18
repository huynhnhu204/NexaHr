package com.nexahr.entity;

import com.nexahr.entity.enums.CompanyStatus;
import com.nexahr.entity.enums.DataRegion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String logo;

    private String address;

    private Double latitude;

    private Double longitude;

    @Column(name = "attendance_radius_meters")
    @Builder.Default
    private Integer attendanceRadiusMeters = 300;

    private String phone;

    private String website;

    @Column(name = "primary_color")
    @Builder.Default
    private String primaryColor = "#1E3A8A";

    @Column(name = "careers_tagline")
    private String careersTagline;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(nullable = false)
    @Builder.Default
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String locale = "vi";

    @Enumerated(EnumType.STRING)
    @Column(name = "data_region", nullable = false)
    @Builder.Default
    private DataRegion dataRegion = DataRegion.AP_SOUTHEAST;

    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private boolean onboardingCompleted = false;

    @Column(name = "onboarding_step", nullable = false)
    @Builder.Default
    private int onboardingStep = 0;

    @Column(nullable = false)
    @Builder.Default
    private String plan = "FREE";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
