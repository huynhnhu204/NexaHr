package com.nexahr.service;

public interface PayrollExportService {
    byte[] exportToExcel(Long payrollId);
    byte[] exportToPdf(Long payrollId);
}
