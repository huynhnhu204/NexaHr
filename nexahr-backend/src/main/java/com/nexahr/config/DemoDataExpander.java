package com.nexahr.config;

import com.nexahr.entity.*;
import com.nexahr.entity.enums.*;
import com.nexahr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bổ sung dữ liệu demo phong phú cho NexaHR Demo (30 nhân viên, biểu đồ có số liệu).
 * Chạy khi số nhân viên công ty demo &lt; 30 — không xóa dữ liệu hiện có.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataExpander {

    private static final int TARGET_EMPLOYEES = 30;

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final CourseRepository courseRepository;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final AssetRepository assetRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    private static final String[][] STAFF = {
            {"Trần Minh Tuấn", "EMP0005"}, {"Lê Thị Hương", "EMP0006"}, {"Phạm Quốc Bảo", "EMP0007"},
            {"Hoàng Văn Đức", "EMP0008"}, {"Võ Thị Mai", "EMP0009"}, {"Đặng Văn Hùng", "EMP0010"},
            {"Bùi Thị Lan", "EMP0011"}, {"Ngô Văn Phúc", "EMP0012"}, {"Dương Thị Ngọc", "EMP0013"},
            {"Lý Văn Thắng", "EMP0014"}, {"Mai Thị Hà", "EMP0015"}, {"Trương Văn Kiệt", "EMP0016"},
            {"Đinh Thị Oanh", "EMP0017"}, {"Hồ Văn Long", "EMP0018"}, {"Vũ Thị Thảo", "EMP0019"},
            {"Cao Văn Nam", "EMP0020"}, {"Lương Thị Hồng", "EMP0021"}, {"Tô Văn Sơn", "EMP0022"},
            {"Chu Thị Yến", "EMP0023"}, {"Phan Văn Khôi", "EMP0024"}, {"Quách Thị Linh", "EMP0025"},
            {"Tạ Văn Bình", "EMP0026"}, {"Hà Thị Nhung", "EMP0027"}, {"Lâm Văn Tài", "EMP0028"},
            {"Đỗ Thị Phương", "EMP0029"}, {"Vương Văn Hiếu", "EMP0030"},
    };

    @Transactional
    public void expandIfNeeded() {
        Company company = companyRepository.findByCode("NEXA-DEMO").orElse(null);
        if (company == null) return;

        long count = employeeRepository.countByCompanyId(company.getId());
        if (count >= TARGET_EMPLOYEES) {
            return;
        }

        log.info("Expanding NexaHR Demo data (current employees: {})...", count);

        Department hr = findDept(company, "Human Resources");
        Department it = findDept(company, "Engineering");
        Department finance = findDept(company, "Finance");
        Department marketing = ensureDept(company, "Marketing", "Phòng Marketing & Truyền thông");
        Department sales = ensureDept(company, "Sales", "Phòng Kinh doanh");

        Position developer = findPosition(company, "Software Developer");
        Position accountant = findPosition(company, "Accountant");
        Position marketingSpec = ensurePosition(company, "Marketing Specialist", BigDecimal.valueOf(16_000_000));
        Position salesExec = ensurePosition(company, "Sales Executive", BigDecimal.valueOf(17_000_000));
        Position qaEngineer = ensurePosition(company, "QA Engineer", BigDecimal.valueOf(15_000_000));

        List<Position> positions = List.of(developer, accountant, marketingSpec, salesExec, qaEngineer);
        List<Department> departments = List.of(it, it, marketing, sales, finance, hr);

        List<Employee> newEmployees = new ArrayList<>();
        for (int i = 0; i < STAFF.length; i++) {
            if (employeeRepository.findByEmployeeCode(STAFF[i][1]).isPresent()) continue;
            Department dept = departments.get(i % departments.size());
            Position pos = positions.get(i % positions.size());
            Employee emp = employeeRepository.save(Employee.builder()
                    .company(company)
                    .employeeCode(STAFF[i][1])
                    .fullName(STAFF[i][0])
                    .gender(i % 2 == 0 ? "Nam" : "Nữ")
                    .phone("09" + String.format("%08d", 10000000 + i))
                    .personalEmail(slug(STAFF[i][0]) + "@nexahr-demo.com")
                    .address("TP. Hồ Chí Minh, Việt Nam")
                    .department(dept)
                    .position(pos)
                    .hireDate(LocalDate.now().minusMonths(3 + (i % 18)))
                    .employmentStatus(i == STAFF.length - 1 ? EmploymentStatus.PROBATION : EmploymentStatus.ACTIVE)
                    .annualLeaveBalance(12 - (i % 4))
                    .build());
            newEmployees.add(emp);
        }

        seedExtraJobs(company, marketing, sales, developer, marketingSpec, salesExec);
        seedExtraLeaves(newEmployees);
        seedExtraAttendance(newEmployees);
        seedExtraPayrolls(newEmployees);
        seedExtraCandidates(company);
        seedExtraCourses(company);
        seedExtraAssets(company);
        seedExtraNotifications();
        seedExtraActivityLogs();

        log.info("Demo expansion complete — {} employees in NexaHR Demo", employeeRepository.countByCompanyId(company.getId()));
    }

    private Department findDept(Company company, String name) {
        return departmentRepository.findByCompanyId(company.getId()).stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Department missing: " + name));
    }

    private Department ensureDept(Company company, String name, String desc) {
        return departmentRepository.findByCompanyId(company.getId()).stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .company(company).name(name).description(desc).build()));
    }

    private Position findPosition(Company company, String name) {
        return positionRepository.findByCompanyId(company.getId()).stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Position missing: " + name));
    }

    private Position ensurePosition(Company company, String name, BigDecimal salary) {
        return positionRepository.findByCompanyId(company.getId()).stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseGet(() -> positionRepository.save(Position.builder()
                        .company(company).name(name).baseSalary(salary)
                        .description("Vị trí " + name).build()));
    }

    private String slug(String name) {
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9]", ".")
                .replaceAll("\\.+", ".");
    }

    private void seedExtraJobs(Company company, Department marketing, Department sales,
                               Position dev, Position mkt, Position salesPos) {
        if (jobPostingRepository.findByCompanyId(company.getId()).size() >= 8) return;

        String[][] jobs = {
                {"Chuyên viên Marketing Digital", "Marketing", "1+ năm kinh nghiệm digital marketing"},
                {"Nhân viên Kinh doanh B2B", "Sales", "Kỹ năng đàm phán, tiếng Anh giao tiếp"},
                {"Lập trình viên React", "Engineering", "React, TypeScript, 2+ năm"},
                {"Kế toán tổng hợp", "Finance", "Am hiểu luật thuế VN"},
                {"Chuyên viên QA/Tester", "Engineering", "Kiểm thử manual & automation"},
                {"Trưởng nhóm Sales", "Sales", "3+ năm quản lý đội sales"},
        };

        for (String[] job : jobs) {
            Department dept = job[1].equals("Marketing") ? marketing
                    : job[1].equals("Sales") ? sales
                    : job[1].equals("Finance") ? findDept(company, "Finance")
                    : findDept(company, "Engineering");
            Position pos = job[1].equals("Marketing") ? mkt
                    : job[1].equals("Sales") ? salesPos : dev;

            jobPostingRepository.save(JobPosting.builder()
                    .company(company)
                    .title(job[0])
                    .department(dept)
                    .position(pos)
                    .description("Cơ hội nghề nghiệp tại NexaHR Demo — " + job[0])
                    .requirement(job[2])
                    .salaryRange("15.000.000 - 25.000.000 VND")
                    .status(JobStatus.OPEN)
                    .publishedToCareers(true)
                    .build());
        }
    }

    private void seedExtraLeaves(List<Employee> employees) {
        if (leaveRepository.count() >= 10) return;
        LeaveStatus[] statuses = {LeaveStatus.PENDING, LeaveStatus.APPROVED, LeaveStatus.REJECTED, LeaveStatus.APPROVED};
        LeaveType[] types = {LeaveType.ANNUAL_LEAVE, LeaveType.SICK_LEAVE, LeaveType.UNPAID_LEAVE, LeaveType.ANNUAL_LEAVE};

        for (int i = 0; i < Math.min(7, employees.size()); i++) {
            Employee emp = employees.get(i);
            int days = 1 + (i % 3);
            LocalDate start = LocalDate.now().plusDays(5 + i * 2);
            leaveRepository.save(LeaveRequest.builder()
                    .employee(emp)
                    .leaveType(types[i % types.length])
                    .startDate(start)
                    .endDate(start.plusDays(days - 1))
                    .totalDays(days)
                    .reason(switch (i % 4) {
                        case 0 -> "Nghỉ phép gia đình";
                        case 1 -> "Khám bệnh định kỳ";
                        case 2 -> "Việc cá nhân";
                        default -> "Du lịch ngắn ngày";
                    })
                    .status(statuses[i % statuses.length])
                    .build());
        }
    }

    private void seedExtraAttendance(List<Employee> employees) {
        LocalDate today = LocalDate.now();
        for (Employee emp : employees) {
            for (int d = 0; d < 10; d++) {
                LocalDate workDate = today.minusDays(d);
                if (workDate.getDayOfWeek().getValue() >= 6) continue;
                if (attendanceRepository.findByEmployeeIdAndWorkDate(emp.getId(), workDate).isPresent()) continue;

                attendanceRepository.save(Attendance.builder()
                        .employee(emp)
                        .workDate(workDate)
                        .checkInTime(workDate.atTime(8, 30).plusMinutes(ThreadLocalRandom.current().nextInt(0, 20)))
                        .checkOutTime(workDate.atTime(17, 30))
                        .totalHours(BigDecimal.valueOf(8.0))
                        .status(d == 1 ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME)
                        .build());
            }
        }
    }

    private void seedExtraPayrolls(List<Employee> employees) {
        for (Employee emp : employees) {
            BigDecimal base = emp.getPosition() != null && emp.getPosition().getBaseSalary() != null
                    ? emp.getPosition().getBaseSalary() : BigDecimal.valueOf(15_000_000);
            for (int m = 0; m < 2; m++) {
                LocalDate month = LocalDate.now().minusMonths(m).withDayOfMonth(1);
                String salaryMonth = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                if (payrollRepository.findByEmployeeIdAndSalaryMonth(emp.getId(), salaryMonth).isPresent()) continue;

                BigDecimal gross = base.multiply(BigDecimal.valueOf(1.1));
                BigDecimal deduction = gross.multiply(BigDecimal.valueOf(0.12));
                payrollRepository.save(Payroll.builder()
                        .employee(emp)
                        .salaryMonth(salaryMonth)
                        .baseSalary(base)
                        .allowance(BigDecimal.valueOf(1_500_000))
                        .bonus(m == 0 ? BigDecimal.valueOf(1_000_000) : BigDecimal.ZERO)
                        .deduction(deduction)
                        .workingDays(22)
                        .standardWorkingDays(22)
                        .actualWorkingDays(21)
                        .grossIncome(gross)
                        .totalDeduction(deduction)
                        .netSalary(gross.subtract(deduction).setScale(0, RoundingMode.HALF_UP))
                        .status(m == 0 ? PayrollStatus.DRAFT : PayrollStatus.PAID)
                        .build());
            }
        }
    }

    private void seedExtraCandidates(Company company) {
        if (candidateRepository.count() >= 20) return;
        List<JobPosting> jobs = jobPostingRepository.findByCompanyId(company.getId());
        if (jobs.isEmpty()) return;

        String[][] cands = {
                {"Nguyễn Thành Đạt", "dat.nguyen@email.com", "INTERVIEW"},
                {"Trần Kim Chi", "chi.tran@email.com", "NEW"},
                {"Lê Hoàng Nam", "nam.le@email.com", "SCREENING"},
                {"Phạm Thu Hà", "ha.pham@email.com", "HIRED"},
                {"Võ Minh Quân", "quan.vo@email.com", "NEW"},
                {"Hoàng Thị Trang", "trang.hoang@email.com", "OFFERED"},
                {"Đỗ Văn Phong", "phong.do@email.com", "REJECTED"},
                {"Bùi Anh Tuấn", "tuan.bui@email.com", "INTERVIEW"},
        };

        CandidateStatus[] statusMap = {
                CandidateStatus.INTERVIEW, CandidateStatus.NEW, CandidateStatus.SCREENING,
                CandidateStatus.HIRED, CandidateStatus.NEW, CandidateStatus.OFFERED,
                CandidateStatus.REJECTED, CandidateStatus.INTERVIEW
        };

        for (int i = 0; i < cands.length; i++) {
            candidateRepository.save(Candidate.builder()
                    .jobPosting(jobs.get(i % jobs.size()))
                    .fullName(cands[i][0])
                    .email(cands[i][1])
                    .phone("09" + (80000000 + i))
                    .status(statusMap[i])
                    .note("Ứng viên từ careers portal")
                    .build());
        }

        if (interviewRepository.count() < 6) {
            List<Candidate> all = candidateRepository.findAll();
            Employee interviewer = employeeRepository.findByEmployeeCode("EMP0003").orElse(null);
            if (interviewer != null) {
                for (int i = 0; i < Math.min(4, all.size()); i++) {
                    interviewRepository.save(Interview.builder()
                            .candidate(all.get(i))
                            .interviewer(interviewer)
                            .scheduledAt(LocalDateTime.now().plusDays(i + 1).with(LocalTime.of(9 + i, 0)))
                            .duration(45)
                            .mode(i % 2 == 0 ? InterviewMode.ONLINE : InterviewMode.OFFLINE)
                            .location(i % 2 == 0 ? null : "VP NexaHR Demo, Q1")
                            .meetingLink(i % 2 == 0 ? "https://meet.google.com/nexahr-" + i : null)
                            .status(InterviewStatus.SCHEDULED)
                            .notes("Phỏng vấn vòng " + (i + 1))
                            .build());
                }
            }
        }
    }

    private void seedExtraCourses(Company company) {
        if (courseRepository.count() >= 5) return;
        String[][] courses = {
                {"Kỹ năng giao tiếp nơi công sở", "Nguyen Thi HR"},
                {"Excel nâng cao cho HR", "Tran Van Manager"},
                {"An toàn thông tin cơ bản", "Admin User"},
        };
        for (String[] c : courses) {
            Course course = courseRepository.save(Course.builder()
                    .title(c[0])
                    .description("Khóa đào tạo nội bộ — " + c[0])
                    .instructor(c[1])
                    .startDate(LocalDate.now().plusDays(14))
                    .endDate(LocalDate.now().plusDays(21))
                    .maxParticipants(30)
                    .status(CourseStatus.ACTIVE)
                    .build());

            List<Employee> staff = employeeRepository.findActiveByCompanyId(company.getId());
            for (int i = 0; i < Math.min(3, staff.size()); i++) {
                trainingEnrollmentRepository.save(TrainingEnrollment.builder()
                        .course(course)
                        .employee(staff.get(i))
                        .status(i == 0 ? EnrollmentStatus.COMPLETED : EnrollmentStatus.ENROLLED)
                        .score(i == 0 ? 85 : null)
                        .enrolledAt(LocalDateTime.now().minusDays(3))
                        .build());
            }
        }
    }

    private void seedExtraAssets(Company company) {
        if (assetRepository.count() >= 15) return;
        AssetType[] types = {AssetType.LAPTOP, AssetType.MONITOR, AssetType.PHONE, AssetType.OTHER};
        for (int i = 1; i <= 12; i++) {
            assetRepository.save(Asset.builder()
                    .name("Thiết bị demo #" + i)
                    .assetCode(String.format("AST-DEM-%03d", i))
                    .assetType(types[i % types.length])
                    .description("Tài sản demo seed")
                    .purchaseDate(LocalDate.now().minusMonths(6 + (i % 12)))
                    .purchasePrice(BigDecimal.valueOf(5_000_000L * (i % 5 + 1)))
                    .status(i % 3 == 0 ? AssetStatus.ASSIGNED : AssetStatus.AVAILABLE)
                    .build());
        }
    }

    private void seedExtraNotifications() {
        if (notificationRepository.count() >= 20) return;
        userRepository.findByUsername("admin").ifPresent(admin -> {
            for (int i = 0; i < 10; i++) {
                notificationRepository.save(Notification.builder()
                        .user(admin)
                        .title("Thông báo hệ thống #" + (i + 1))
                        .message("Cập nhật chính sách HR — vui lòng xem bảng tin công ty.")
                        .type(NotificationType.GENERAL)
                        .isRead(i > 5)
                        .build());
            }
        });
    }

    private void seedExtraActivityLogs() {
        if (activityLogRepository.count() >= 30) return;
        userRepository.findByUsername("admin").ifPresent(admin -> {
            String[] actions = {"LOGIN", "CREATE", "UPDATE", "VIEW", "EXPORT"};
            String[] modules = {"AUTH", "EMPLOYEE", "LEAVE", "PAYROLL", "REPORT"};
            for (int i = 0; i < 20; i++) {
                activityLogRepository.save(ActivityLog.builder()
                        .user(admin)
                        .action(actions[i % actions.length])
                        .module(modules[i % modules.length])
                        .description("Hoạt động demo #" + (i + 1))
                        .ipAddress("192.168.1." + (10 + i % 50))
                        .build());
            }
        });
    }
}
