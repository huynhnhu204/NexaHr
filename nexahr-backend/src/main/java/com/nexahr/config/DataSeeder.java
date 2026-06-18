package com.nexahr.config;

import com.nexahr.entity.*;
import com.nexahr.entity.enums.*;
import com.nexahr.repository.*;
import com.nexahr.service.CustomRoleService;
import com.nexahr.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final ActivityLogRepository activityLogRepository;
    private final JobPostingRepository jobPostingRepository;
    private final WorkflowRuleRepository workflowRuleRepository;
    private final PermissionService permissionService;
    private final CustomRoleService customRoleService;
    private final AuditLogRepository auditLogRepository;
    private final SamlConfigRepository samlConfigRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final AssetRepository assetRepository;
    private final AssetAssignmentHistoryRepository assetAssignmentHistoryRepository;
    private final CourseRepository courseRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final BillingInvoiceRepository billingInvoiceRepository;
    private final ScheduledReportRepository scheduledReportRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoDataExpander demoDataExpander;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding core data...");
            seedCore();
        }

        companyRepository.findByCode("NEXA-DEMO").ifPresent(company -> {
            if (leaveRepository.count() == 0) {
                log.info("Seeding feature demo data...");
                seedFeatureData(company);
                log.info("Feature demo data seeded successfully");
            } else {
                log.info("Feature data already exists, skipping feature seed...");
            }
        });
        companyRepository.findByCode("NEXA-LABS").ifPresent(this::seedLabsBasics);
        companyRepository.findByCode("NEXA-DEMO").ifPresent(c -> demoDataExpander.expandIfNeeded());
        patchCompanyAttendanceLocations();
    }

    private static final String DEMO_OFFICE_ADDRESS = "53A Tăng Nhơn Phú, Phường Phước Long B, TP. Thủ Đức, TP.HCM";
    private static final double DEMO_OFFICE_LAT = 10.8277714;
    private static final double DEMO_OFFICE_LON = 106.7715260;

    private void patchCompanyAttendanceLocations() {
        companyRepository.findByCode("NEXA-DEMO").ifPresent(c -> {
            c.setAddress(DEMO_OFFICE_ADDRESS);
            c.setLatitude(DEMO_OFFICE_LAT);
            c.setLongitude(DEMO_OFFICE_LON);
            if (c.getAttendanceRadiusMeters() == null) {
                c.setAttendanceRadiusMeters(300);
            }
            companyRepository.save(c);
        });
        companyRepository.findByCode("NEXA-LABS").ifPresent(c -> {
            if (c.getLatitude() == null) {
                c.setLatitude(21.0285);
                c.setLongitude(105.8542);
                c.setAttendanceRadiusMeters(300);
                companyRepository.save(c);
            }
        });
    }

    private void seedLabsBasics(Company labsCompany) {
        if (!positionRepository.findByCompanyId(labsCompany.getId()).isEmpty()) {
            return;
        }
        log.info("Seeding NexaHR Labs positions...");
        departmentRepository.save(Department.builder()
                .company(labsCompany)
                .name("R&D")
                .description("Research & Development")
                .build());
        positionRepository.save(Position.builder()
                .company(labsCompany)
                .name("Lab Engineer")
                .baseSalary(BigDecimal.valueOf(20_000_000))
                .description("Kỹ sư phòng lab")
                .build());
        positionRepository.save(Position.builder()
                .company(labsCompany)
                .name("Product Analyst")
                .baseSalary(BigDecimal.valueOf(18_000_000))
                .description("Phân tích sản phẩm")
                .build());
    }

    private void seedCore() {
        Company demoCompany = companyRepository.save(Company.builder()
                .name("NexaHR Demo")
                .code("NEXA-DEMO")
                .address(DEMO_OFFICE_ADDRESS)
                .latitude(DEMO_OFFICE_LAT)
                .longitude(DEMO_OFFICE_LON)
                .attendanceRadiusMeters(300)
                .phone("028 1234 5678")
                .website("https://nexahr.com")
                .careersTagline("Cơ hội nghề nghiệp — Gia nhập đội ngũ của chúng tôi")
                .primaryColor("#1E3A8A")
                .billingEmail("billing@nexahr.com")
                .plan("PRO")
                .status(CompanyStatus.ACTIVE)
                .onboardingCompleted(true)
                .onboardingStep(4)
                .build());

        Company labsCompany = companyRepository.save(Company.builder()
                .name("NexaHR Labs")
                .code("NEXA-LABS")
                .address("Ha Noi, Vietnam")
                .latitude(21.0285)
                .longitude(105.8542)
                .attendanceRadiusMeters(300)
                .plan("FREE")
                .status(CompanyStatus.ACTIVE)
                .onboardingCompleted(false)
                .onboardingStep(0)
                .build());

        Position ceo = positionRepository.save(Position.builder().company(demoCompany).name("CEO").baseSalary(BigDecimal.valueOf(50000000)).description("Chief Executive Officer").build());
        Position hrManager = positionRepository.save(Position.builder().company(demoCompany).name("HR Manager").baseSalary(BigDecimal.valueOf(25000000)).description("Human Resources Manager").build());
        Position devLead = positionRepository.save(Position.builder().company(demoCompany).name("Development Lead").baseSalary(BigDecimal.valueOf(30000000)).description("Team Lead Developer").build());
        Position developer = positionRepository.save(Position.builder().company(demoCompany).name("Software Developer").baseSalary(BigDecimal.valueOf(18000000)).description("Full-stack Developer").build());
        positionRepository.save(Position.builder().company(demoCompany).name("Accountant").baseSalary(BigDecimal.valueOf(15000000)).description("Finance Accountant").build());

        Department executive = departmentRepository.save(Department.builder().company(demoCompany).name("Executive").description("Executive Board").build());
        Department hr = departmentRepository.save(Department.builder().company(demoCompany).name("Human Resources").description("HR Department").build());
        Department engineering = departmentRepository.save(Department.builder().company(demoCompany).name("Engineering").description("Software Engineering").build());
        departmentRepository.save(Department.builder().company(demoCompany).name("Finance").description("Finance & Accounting").build());

        Employee adminEmployee = createUser("admin", "admin@nexahr.com", "123456", Role.ADMIN, "Admin User", "EMP0001", demoCompany, executive, ceo, EmploymentStatus.ACTIVE);
        createUser("hr", "hr@nexahr.com", "123456", Role.HR, "Nguyen Thi HR", "EMP0002", demoCompany, hr, hrManager, EmploymentStatus.ACTIVE);
        Employee manager = createUser("manager", "manager@nexahr.com", "123456", Role.MANAGER, "Tran Van Manager", "EMP0003", demoCompany, engineering, devLead, EmploymentStatus.ACTIVE);
        Employee employeeUser = createUser("employee", "employee@nexahr.com", "123456", Role.EMPLOYEE, "Le Thi Employee", "EMP0004", demoCompany, engineering, developer, EmploymentStatus.ACTIVE);
        employeeUser.setManager(manager);
        employeeRepository.save(employeeUser);

        engineering.setManager(manager);
        departmentRepository.save(engineering);

        for (User user : userRepository.findAll()) {
            membershipRepository.save(CompanyMembership.builder()
                    .user(user)
                    .company(demoCompany)
                    .isDefault(true)
                    .build());
        }

        membershipRepository.save(CompanyMembership.builder()
                .user(adminEmployee.getUser())
                .company(labsCompany)
                .isDefault(false)
                .build());

        seedActivityLogs();
        seedJobPostings(demoCompany, engineering, developer, hr, hrManager);
        seedWorkflowRules(demoCompany);
        permissionService.seedDefaults(demoCompany);
        permissionService.seedDefaults(labsCompany);
        customRoleService.seedDemoRoles(demoCompany);
        seedSamlConfig(demoCompany);
        seedAuditLogs();

        log.info("Core data seeded — demo accounts: admin@nexahr.com / 123456");
    }

    private void seedFeatureData(Company company) {
        Employee admin = requireEmployee("EMP0001");
        Employee hrEmp = requireEmployee("EMP0002");
        Employee manager = requireEmployee("EMP0003");
        Employee employee = requireEmployee("EMP0004");
        User adminUser = admin.getUser();
        User hrUser = hrEmp.getUser();
        User managerUser = manager.getUser();
        User employeeUser = employee.getUser();

        seedLeaveRequests(employee, managerUser, hrUser);
        seedAttendance(employee, manager, admin);
        seedPayrolls(employee, manager, hrEmp, admin);
        seedPerformanceReviews(employee, manager);
        seedRecruitment(company, manager);
        seedAssets(employee, manager, adminUser);
        seedTraining(employee, manager);
        seedAnnouncements(company, adminUser, hrUser);
        seedNotifications(adminUser, managerUser, employeeUser);
        seedEmployeeDocuments(employee, adminUser);
        seedApiKeys(company);
        seedWebhooks(company);
        seedBilling(company);
        seedScheduledReports(company);
        seedPushDevices(adminUser, employeeUser);
    }

    private void seedLeaveRequests(Employee employee, User managerUser, User hrUser) {
        leaveRepository.save(LeaveRequest.builder()
                .employee(employee)
                .leaveType(LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .totalDays(3)
                .reason("Nghỉ phép cuối năm")
                .status(LeaveStatus.PENDING)
                .build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(employee)
                .leaveType(LeaveType.SICK_LEAVE)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(5))
                .totalDays(1)
                .reason("Ốm sốt")
                .status(LeaveStatus.APPROVED)
                .approvedBy(managerUser)
                .approvedAt(LocalDateTime.now().minusDays(6))
                .build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(employee)
                .leaveType(LeaveType.UNPAID_LEAVE)
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().minusDays(18))
                .totalDays(3)
                .reason("Việc gia đình")
                .status(LeaveStatus.REJECTED)
                .approvedBy(hrUser)
                .approvedAt(LocalDateTime.now().minusDays(21))
                .rejectReason("Không đủ ngày phép năm")
                .build());
    }

    private void seedAttendance(Employee employee, Employee manager, Employee admin) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate workDate = today.minusDays(i);
            if (workDate.getDayOfWeek().getValue() >= 6) continue;

            for (Employee emp : List.of(employee, manager, admin)) {
                LocalDateTime checkIn = workDate.atTime(8, 30).plusMinutes(i % 3 * 5L);
                LocalDateTime checkOut = workDate.atTime(17, 30);
                attendanceRepository.save(Attendance.builder()
                        .employee(emp)
                        .company(emp.getCompany())
                        .workDate(workDate)
                        .checkInTime(checkIn)
                        .checkOutTime(checkOut)
                        .totalHours(BigDecimal.valueOf(8.0))
                        .status(i == 1 && emp.equals(employee) ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME)
                        .note(i == 1 && emp.equals(employee) ? "Đến muộn 15 phút" : null)
                        .build());
            }
        }
    }

    private void seedPayrolls(Employee employee, Employee manager, Employee hrEmp, Employee admin) {
        for (Employee emp : List.of(employee, manager, hrEmp, admin)) {
            BigDecimal base = emp.getPosition().getBaseSalary();
            for (int monthOffset = 0; monthOffset < 3; monthOffset++) {
                LocalDate month = LocalDate.now().minusMonths(monthOffset).withDayOfMonth(1);
                String salaryMonth = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                BigDecimal allowance = BigDecimal.valueOf(1_500_000);
                BigDecimal bonus = monthOffset == 0 ? BigDecimal.valueOf(2_000_000) : BigDecimal.ZERO;
                BigDecimal deduction = BigDecimal.valueOf(500_000);
                BigDecimal gross = base.add(allowance).add(bonus);
                BigDecimal insurance = base.multiply(BigDecimal.valueOf(0.105));
                BigDecimal tax = gross.multiply(BigDecimal.valueOf(0.05));
                BigDecimal totalDeduction = deduction.add(insurance).add(tax);
                payrollRepository.save(Payroll.builder()
                        .employee(emp)
                        .salaryMonth(salaryMonth)
                        .baseSalary(base)
                        .allowance(allowance)
                        .bonus(bonus)
                        .deduction(deduction)
                        .workingDays(22)
                        .standardWorkingDays(22)
                        .actualWorkingDays(22 - monthOffset)
                        .overtimeHours(BigDecimal.valueOf(monthOffset == 0 ? 4 : 0))
                        .overtimePay(BigDecimal.valueOf(monthOffset == 0 ? 800_000 : 0))
                        .socialInsurance(insurance.multiply(BigDecimal.valueOf(0.08)))
                        .healthInsurance(insurance.multiply(BigDecimal.valueOf(0.015)))
                        .unemploymentInsurance(insurance.multiply(BigDecimal.valueOf(0.01)))
                        .personalIncomeTax(tax)
                        .grossIncome(gross)
                        .totalDeduction(totalDeduction)
                        .netSalary(gross.subtract(totalDeduction).setScale(0, RoundingMode.HALF_UP))
                        .status(monthOffset == 0 ? PayrollStatus.DRAFT : PayrollStatus.PAID)
                        .build());
            }
        }
    }

    private void seedPerformanceReviews(Employee employee, Employee manager) {
        performanceReviewRepository.save(PerformanceReview.builder()
                .employee(employee)
                .reviewer(manager)
                .reviewPeriod("Q1 2026")
                .goals("Hoàn thành module Performance Review, cải thiện code coverage")
                .dueDate(LocalDate.now().plusDays(14))
                .status(PerformanceReviewStatus.PENDING_SELF)
                .build());

        performanceReviewRepository.save(PerformanceReview.builder()
                .employee(employee)
                .reviewer(manager)
                .reviewPeriod("Q4 2025")
                .goals("Triển khai Data Hub và tích hợp API")
                .dueDate(LocalDate.now().minusDays(30))
                .status(PerformanceReviewStatus.COMPLETED)
                .employeeSelfComment("Đã hoàn thành đúng tiến độ, học thêm Spring Security")
                .score(BigDecimal.valueOf(4.2))
                .rating(PerformanceRating.EXCELLENT)
                .comment("Nhân viên chủ động, chất lượng code tốt")
                .build());

        performanceReviewRepository.save(PerformanceReview.builder()
                .employee(manager)
                .reviewer(requireEmployee("EMP0001"))
                .reviewPeriod("Q1 2026")
                .goals("Dẫn dắt team Engineering, mentoring 2 junior dev")
                .dueDate(LocalDate.now().plusDays(21))
                .status(PerformanceReviewStatus.DRAFT)
                .build());
    }

    private void seedRecruitment(Company company, Employee interviewer) {
        List<JobPosting> jobs = jobPostingRepository.findAll();
        if (jobs.isEmpty()) return;

        JobPosting devJob = jobs.get(0);

        Candidate cand1 = candidateRepository.save(Candidate.builder()
                .jobPosting(devJob)
                .fullName("Pham Van Dev")
                .email("pham.dev@email.com")
                .phone("0912345678")
                .cvFile("uploads/cv/pham-dev.pdf")
                .status(CandidateStatus.INTERVIEW)
                .note("Kinh nghiệm 3 năm Java/React")
                .build());

        Candidate cand2 = candidateRepository.save(Candidate.builder()
                .jobPosting(devJob)
                .fullName("Hoang Thi QA")
                .email("hoang.qa@email.com")
                .phone("0987654321")
                .status(CandidateStatus.NEW)
                .note("Ứng viên mới nộp CV")
                .build());

        Candidate cand3 = candidateRepository.save(Candidate.builder()
                .jobPosting(jobs.size() > 1 ? jobs.get(1) : devJob)
                .fullName("Vo Minh HR")
                .email("vo.hr@email.com")
                .phone("0909090909")
                .status(CandidateStatus.OFFERED)
                .note("Đã gửi offer letter")
                .build());

        interviewRepository.save(Interview.builder()
                .candidate(cand1)
                .interviewer(interviewer)
                .scheduledAt(LocalDateTime.now().plusDays(2).with(LocalTime.of(14, 0)))
                .duration(60)
                .mode(InterviewMode.ONLINE)
                .meetingLink("https://meet.google.com/nexahr-demo")
                .status(InterviewStatus.SCHEDULED)
                .notes("Phỏng vấn vòng 1 — Technical")
                .build());

        interviewRepository.save(Interview.builder()
                .candidate(cand3)
                .interviewer(interviewer)
                .scheduledAt(LocalDateTime.now().minusDays(3).with(LocalTime.of(10, 0)))
                .duration(45)
                .mode(InterviewMode.OFFLINE)
                .location("Tầng 5, VP HCM")
                .status(InterviewStatus.COMPLETED)
                .evaluation("Ứng viên phù hợp văn hóa công ty, kỹ năng giao tiếp tốt")
                .notes("Đề xuất offer")
                .build());
    }

    private void seedAssets(Employee employee, Employee manager, User adminUser) {
        Asset laptop = assetRepository.save(Asset.builder()
                .name("MacBook Pro 14\"")
                .assetCode("AST-LAP-001")
                .assetType(AssetType.LAPTOP)
                .description("Máy phát triển cho Engineering")
                .purchaseDate(LocalDate.of(2024, 6, 1))
                .purchasePrice(BigDecimal.valueOf(45_000_000))
                .status(AssetStatus.ASSIGNED)
                .assignedTo(employee)
                .assignedAt(LocalDateTime.now().minusMonths(6))
                .build());

        Asset monitor = assetRepository.save(Asset.builder()
                .name("Dell UltraSharp 27\"")
                .assetCode("AST-MON-001")
                .assetType(AssetType.MONITOR)
                .description("Màn hình phụ")
                .purchaseDate(LocalDate.of(2024, 6, 1))
                .purchasePrice(BigDecimal.valueOf(12_000_000))
                .status(AssetStatus.ASSIGNED)
                .assignedTo(manager)
                .assignedAt(LocalDateTime.now().minusMonths(5))
                .build());

        assetRepository.save(Asset.builder()
                .name("iPhone 15 Pro")
                .assetCode("AST-PHN-001")
                .assetType(AssetType.PHONE)
                .description("Điện thoại công ty dự phòng")
                .purchaseDate(LocalDate.of(2025, 1, 10))
                .purchasePrice(BigDecimal.valueOf(28_000_000))
                .status(AssetStatus.AVAILABLE)
                .build());

        assetAssignmentHistoryRepository.save(AssetAssignmentHistory.builder()
                .asset(laptop)
                .employee(employee)
                .assignedAt(LocalDateTime.now().minusMonths(6))
                .assignedBy(adminUser)
                .note("Bàn giao khi onboard")
                .build());

        assetAssignmentHistoryRepository.save(AssetAssignmentHistory.builder()
                .asset(monitor)
                .employee(manager)
                .assignedAt(LocalDateTime.now().minusMonths(5))
                .assignedBy(adminUser)
                .note("Nâng cấp thiết bị quản lý")
                .build());
    }

    private void seedTraining(Employee employee, Employee manager) {
        Course springCourse = courseRepository.save(Course.builder()
                .title("Spring Boot nâng cao")
                .description("JPA, Security, Testing và triển khai production")
                .instructor("Nguyen Thi HR")
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(14))
                .maxParticipants(20)
                .status(CourseStatus.ACTIVE)
                .build());

        Course softSkill = courseRepository.save(Course.builder()
                .title("Kỹ năng giao tiếp & thuyết trình")
                .description("Workshop 2 ngày cho toàn công ty")
                .instructor("External Trainer")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(29))
                .maxParticipants(50)
                .status(CourseStatus.COMPLETED)
                .build());

        trainingEnrollmentRepository.save(TrainingEnrollment.builder()
                .course(springCourse)
                .employee(employee)
                .enrolledAt(LocalDateTime.now().minusDays(2))
                .status(EnrollmentStatus.ENROLLED)
                .build());

        trainingEnrollmentRepository.save(TrainingEnrollment.builder()
                .course(springCourse)
                .employee(manager)
                .enrolledAt(LocalDateTime.now().minusDays(2))
                .status(EnrollmentStatus.ENROLLED)
                .build());

        trainingEnrollmentRepository.save(TrainingEnrollment.builder()
                .course(softSkill)
                .employee(employee)
                .enrolledAt(LocalDateTime.now().minusDays(35))
                .status(EnrollmentStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusDays(29))
                .score(85)
                .build());
    }

    private void seedAnnouncements(Company company, User adminUser, User hrUser) {
        announcementRepository.save(Announcement.builder()
                .company(company)
                .author(adminUser)
                .title("Chào mừng đến với NexaHR Demo")
                .content("Hệ thống HRM đã sẵn sàng với đầy đủ module: nhân sự, chấm công, lương, tuyển dụng, đào tạo và báo cáo.")
                .pinned(true)
                .published(true)
                .build());

        announcementRepository.save(Announcement.builder()
                .company(company)
                .author(hrUser)
                .title("Lịch nghỉ Tết Nguyên Đán 2026")
                .content("Công ty nghỉ từ 28/01 đến 02/02. Vui lòng hoàn tất đơn ngh phép trước ngày 20/01.")
                .pinned(false)
                .published(true)
                .build());
    }

    private void seedNotifications(User adminUser, User managerUser, User employeeUser) {
        notificationRepository.save(Notification.builder()
                .user(employeeUser)
                .title("Đơn nghỉ phép được duyệt")
                .message("Đơn nghỉ ốm ngày " + LocalDate.now().minusDays(5) + " đã được quản lý duyệt.")
                .type(NotificationType.LEAVE_APPROVED)
                .isRead(true)
                .build());

        notificationRepository.save(Notification.builder()
                .user(employeeUser)
                .title("Đánh giá hiệu suất Q1 2026")
                .message("Vui lòng hoàn thành tự đánh giá trước hạn.")
                .type(NotificationType.GENERAL)
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(managerUser)
                .title("Ứng viên mới")
                .message("Pham Van Dev đã nộp hồ sơ cho vị trí Software Developer.")
                .type(NotificationType.GENERAL)
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(adminUser)
                .title("Bảng lương tháng " + LocalDate.now().getMonthValue() + " đã tạo")
                .message("Kiểm tra và phát hành bảng lương trong module Payroll.")
                .type(NotificationType.PAYROLL_PUBLISHED)
                .isRead(false)
                .build());
    }

    private void seedEmployeeDocuments(Employee employee, User adminUser) {
        employeeDocumentRepository.save(EmployeeDocument.builder()
                .employee(employee)
                .fileName("hop-dong-lao-dong.pdf")
                .originalName("Hợp đồng lao động - Le Thi Employee.pdf")
                .filePath("uploads/docs/hop-dong-lao-dong.pdf")
                .fileSize(245_760L)
                .documentType(DocumentType.CONTRACT)
                .uploadedBy(adminUser)
                .build());

        employeeDocumentRepository.save(EmployeeDocument.builder()
                .employee(employee)
                .fileName("cccd-scan.pdf")
                .originalName("CCCD - Le Thi Employee.pdf")
                .filePath("uploads/docs/cccd-scan.pdf")
                .fileSize(128_000L)
                .documentType(DocumentType.NATIONAL_ID)
                .uploadedBy(adminUser)
                .build());
    }

    private void seedApiKeys(Company company) {
        apiKeyRepository.save(ApiKey.builder()
                .company(company)
                .name("Integration Demo Key")
                .keyPrefix("nxk_demo1234")
                .keyHash(passwordEncoder.encode("nxk_demo_secret_key_for_testing"))
                .scopes("employees:read,departments:read,attendance:read")
                .active(true)
                .lastUsedAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build());
    }

    private void seedWebhooks(Company company) {
        WebhookEndpoint endpoint = webhookEndpointRepository.save(WebhookEndpoint.builder()
                .company(company)
                .name("Slack Notifications")
                .url("https://hooks.slack.com/services/demo/nexahr")
                .secret("whsec_demo_secret")
                .events("LEAVE_CREATED,EMPLOYEE_CREATED,PAYROLL_PUBLISHED")
                .active(true)
                .build());

        webhookDeliveryRepository.save(WebhookDelivery.builder()
                .webhookEndpoint(endpoint)
                .event(WebhookEvent.LEAVE_APPROVED)
                .payload("{\"event\":\"leave.approved\",\"employee\":\"Le Thi Employee\"}")
                .statusCode(200)
                .responseBody("ok")
                .success(true)
                .build());

        webhookDeliveryRepository.save(WebhookDelivery.builder()
                .webhookEndpoint(endpoint)
                .event(WebhookEvent.EMPLOYEE_CREATED)
                .payload("{\"event\":\"employee.created\"}")
                .statusCode(500)
                .responseBody("timeout")
                .success(false)
                .build());
    }

    private void seedBilling(Company company) {
        billingInvoiceRepository.save(BillingInvoice.builder()
                .company(company)
                .invoiceNumber("INV-2026-001")
                .plan(SubscriptionPlan.PRO)
                .amount(BigDecimal.valueOf(2_990_000))
                .currency("VND")
                .status(BillingInvoiceStatus.PAID)
                .paidAt(LocalDateTime.now().minusMonths(1))
                .build());

        billingInvoiceRepository.save(BillingInvoice.builder()
                .company(company)
                .invoiceNumber("INV-2026-002")
                .plan(SubscriptionPlan.PRO)
                .amount(BigDecimal.valueOf(2_990_000))
                .currency("VND")
                .status(BillingInvoiceStatus.PENDING)
                .build());
    }

    private void seedScheduledReports(Company company) {
        scheduledReportRepository.save(ScheduledReport.builder()
                .company(company)
                .name("Báo cáo nhân sự hàng tuần")
                .reportType("WORKFORCE")
                .frequency(ReportFrequency.WEEKLY)
                .recipientEmails("hr@nexahr.com,admin@nexahr.com")
                .active(true)
                .lastRunAt(LocalDateTime.now().minusDays(3))
                .build());

        scheduledReportRepository.save(ScheduledReport.builder()
                .company(company)
                .name("Báo cáo chấm công tháng")
                .reportType("ATTENDANCE")
                .frequency(ReportFrequency.MONTHLY)
                .recipientEmails("manager@nexahr.com")
                .active(true)
                .lastRunAt(LocalDateTime.now().minusDays(15))
                .build());
    }

    private void seedPushDevices(User adminUser, User employeeUser) {
        pushDeviceRepository.save(PushDevice.builder()
                .user(employeeUser)
                .deviceToken("fcm_demo_token_employee_mobile")
                .platform(PushPlatform.ANDROID)
                .active(true)
                .lastUsedAt(LocalDateTime.now().minusHours(2))
                .build());

        pushDeviceRepository.save(PushDevice.builder()
                .user(adminUser)
                .deviceToken("fcm_demo_token_admin_ios")
                .platform(PushPlatform.IOS)
                .active(true)
                .lastUsedAt(LocalDateTime.now().minusDays(1))
                .build());
    }

    private Employee requireEmployee(String code) {
        return employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + code));
    }

    private void seedSamlConfig(Company company) {
        samlConfigRepository.save(SamlConfig.builder()
                .company(company)
                .enabled(true)
                .idpName("NexaHR Demo IdP")
                .entityId("https://idp.demo.nexahr.com")
                .ssoUrl("https://idp.demo.nexahr.com/sso")
                .attributeEmail("email")
                .build());
    }

    private void seedAuditLogs() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) return;
        auditLogRepository.save(AuditLog.builder()
                .user(admin).action("LOGIN").entityType("AUTH")
                .details("Đăng nhập hệ thống (seed)").ipAddress("127.0.0.1").build());
        auditLogRepository.save(AuditLog.builder()
                .user(admin).action("UPDATE").entityType("PERMISSION")
                .details("Khởi tạo ma trận phân quyền mặc định").ipAddress("127.0.0.1").build());
        auditLogRepository.save(AuditLog.builder()
                .user(admin).action("APPROVE").entityType("LEAVE").entityId(1L)
                .details("Duyệt đơn nghỉ phép demo").ipAddress("192.168.1.10").build());
    }

    private void seedWorkflowRules(Company company) {
        workflowRuleRepository.save(WorkflowRule.builder()
                .company(company)
                .name("Thông báo quản lý khi có đơn nghỉ phép")
                .trigger(WorkflowTrigger.LEAVE_CREATED)
                .action(WorkflowAction.NOTIFY_MANAGER)
                .active(true)
                .build());

        workflowRuleRepository.save(WorkflowRule.builder()
                .company(company)
                .name("Tự động duyệt nghỉ phép ≤ 2 ngày")
                .trigger(WorkflowTrigger.LEAVE_CREATED)
                .action(WorkflowAction.AUTO_APPROVE_LEAVE_DAYS_LTE)
                .configValue("2")
                .active(true)
                .build());
    }

    private void seedJobPostings(Company company, Department engineering, Position developer,
                                 Department hr, Position hrManager) {
        jobPostingRepository.save(JobPosting.builder()
                .company(company)
                .title("Software Developer")
                .department(engineering)
                .position(developer)
                .description("Chúng tôi đang tìm kiếm lập trình viên full-stack để tham gia đội ngũ Engineering.")
                .requirement("2+ năm kinh nghiệm Java/Spring Boot, React. Có khả năng làm việc nhóm tốt.")
                .salaryRange("15,000,000 - 25,000,000 VND")
                .status(JobStatus.OPEN)
                .publishedToCareers(true)
                .build());

        jobPostingRepository.save(JobPosting.builder()
                .company(company)
                .title("HR Manager")
                .department(hr)
                .position(hrManager)
                .description("Vị trí quản lý nhân sự, phụ trách tuyển dụng và phát triển nguồn nhân lực.")
                .requirement("3+ năm kinh nghiệm HR, am hiểu luật lao động Việt Nam.")
                .salaryRange("20,000,000 - 30,000,000 VND")
                .status(JobStatus.OPEN)
                .publishedToCareers(true)
                .build());
    }

    private void seedActivityLogs() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) return;
        activityLogRepository.save(ActivityLog.builder()
                .user(admin).action("LOGIN").module("AUTH")
                .description("Đăng nhập hệ thống").ipAddress("127.0.0.1").build());
        activityLogRepository.save(ActivityLog.builder()
                .user(admin).action("CREATE").module("EMPLOYEE")
                .description("Thêm nhân viên mới vào hệ thống").ipAddress("127.0.0.1").build());
        activityLogRepository.save(ActivityLog.builder()
                .user(admin).action("UPDATE").module("DEPARTMENT")
                .description("Cập nhật thông tin phòng ban Engineering").ipAddress("127.0.0.1").build());
    }

    private Employee createUser(String username, String email, String password, Role role,
                                 String fullName, String code, Company company, Department dept,
                                 Position pos, EmploymentStatus status) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .company(company)
                .employeeCode(code)
                .fullName(fullName)
                .gender("Other")
                .phone("0901234567")
                .address("Ho Chi Minh City, Vietnam")
                .department(dept)
                .position(pos)
                .hireDate(LocalDate.of(2024, 1, 15))
                .employmentStatus(status)
                .annualLeaveBalance(12)
                .build();
        employeeRepository.save(employee);
        user.setEmployee(employee);
        return employee;
    }
}
