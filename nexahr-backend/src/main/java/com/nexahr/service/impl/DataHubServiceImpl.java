package com.nexahr.service.impl;

import com.nexahr.config.DefaultRolePermissions;
import com.nexahr.dto.request.EmployeeRequest;
import com.nexahr.dto.response.DataImportResultResponse;
import com.nexahr.entity.*;
import com.nexahr.entity.enums.EmploymentStatus;
import com.nexahr.exception.BadRequestException;
import com.nexahr.repository.CompanyRepository;
import com.nexahr.repository.EmployeeRepository;
import com.nexahr.service.DataHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataHubServiceImpl implements DataHubService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeServiceImpl employeeService;

    @Override
    public byte[] exportEmployeesCsv(Long companyId) {
        List<Employee> employees = employeeRepository.findWithFilters(
                companyId, null, null, null,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        StringBuilder csv = new StringBuilder("employeeCode,fullName,email,phone,department,position,hireDate,status\n");
        for (Employee e : employees) {
            csv.append(csvCell(e.getEmployeeCode())).append(',')
                    .append(csvCell(e.getFullName())).append(',')
                    .append(csvCell(e.getUser() != null ? e.getUser().getEmail() : "")).append(',')
                    .append(csvCell(e.getPhone())).append(',')
                    .append(csvCell(e.getDepartment() != null ? e.getDepartment().getName() : "")).append(',')
                    .append(csvCell(e.getPosition() != null ? e.getPosition().getName() : "")).append(',')
                    .append(e.getHireDate() != null ? e.getHireDate() : "").append(',')
                    .append(e.getEmploymentStatus()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] employeeImportTemplate() {
        String template = """
                fullName,email,phone,employeeCode,hireDate,status
                Nguyen Van A,nguyenvana@company.com,0901234567,EMP0100,2026-01-15,ACTIVE
                Tran Thi B,tranthib@company.com,0907654321,EMP0101,2026-02-01,PROBATION
                """;
        return template.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public DataImportResultResponse importEmployees(Long companyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File CSV không hợp lệ");
        }
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BadRequestException("Company not found"));

        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.toLowerCase().contains("fullname")) {
                throw new BadRequestException("CSV phải có cột fullName, email");
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                total++;
                String[] cols = parseCsvLine(line);
                try {
                    EmployeeRequest req = new EmployeeRequest();
                    req.setFullName(value(cols, 0));
                    req.setEmail(value(cols, 1));
                    req.setPhone(value(cols, 2));
                    req.setEmployeeCode(blankToNull(value(cols, 3)));
                    String hireDate = value(cols, 4);
                    if (hireDate != null && !hireDate.isBlank()) {
                        req.setHireDate(LocalDate.parse(hireDate, DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                    String status = value(cols, 5);
                    if (status != null && !status.isBlank()) {
                        req.setEmploymentStatus(EmploymentStatus.valueOf(status.trim().toUpperCase()));
                    }
                    employeeService.create(req);
                    success++;
                } catch (Exception ex) {
                    errors.add("Dòng " + row + ": " + ex.getMessage());
                }
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Không đọc được file CSV: " + ex.getMessage());
        }

        return DataImportResultResponse.builder()
                .totalRows(total)
                .successCount(success)
                .errorCount(errors.size())
                .errors(errors.stream().limit(20).toList())
                .build();
    }

    private String[] parseCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String value(String[] cols, int index) {
        if (index >= cols.length) return null;
        String v = cols[index].trim();
        if (v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        return v.isBlank() ? null : v;
    }

    private String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    private String csvCell(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"")) return "\"" + v + "\"";
        return v;
    }
}
