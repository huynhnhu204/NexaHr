package com.nexahr.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nexahr.entity.Payroll;
import com.nexahr.exception.BadRequestException;
import com.nexahr.exception.ResourceNotFoundException;
import com.nexahr.repository.PayrollRepository;
import com.nexahr.service.PayrollExportService;
import com.nexahr.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PayrollExportServiceImpl implements PayrollExportService {

    private final PayrollRepository payrollRepository;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    public byte[] exportToExcel(Long payrollId) {
        Payroll payroll = findPayrollInTenant(payrollId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payroll");
            int rowIdx = 0;

            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Mục");
            header.createCell(1).setCellValue("Giá trị");

            addRow(sheet, rowIdx++, "Nhân viên", payroll.getEmployee().getFullName());
            addRow(sheet, rowIdx++, "Mã NV", payroll.getEmployee().getEmployeeCode());
            addRow(sheet, rowIdx++, "Tháng lương", payroll.getSalaryMonth());
            addRow(sheet, rowIdx++, "Lương cơ bản", format(payroll.getBaseSalary()));
            addRow(sheet, rowIdx++, "Phụ cấp", format(payroll.getAllowance()));
            addRow(sheet, rowIdx++, "Thưởng", format(payroll.getBonus()));
            addRow(sheet, rowIdx++, "Lương OT", format(payroll.getOvertimePay()));
            addRow(sheet, rowIdx++, "Tổng thu nhập", format(payroll.getGrossIncome()));
            addRow(sheet, rowIdx++, "BHXH (8%)", format(payroll.getSocialInsurance()));
            addRow(sheet, rowIdx++, "BHYT (1.5%)", format(payroll.getHealthInsurance()));
            addRow(sheet, rowIdx++, "BHTN (1%)", format(payroll.getUnemploymentInsurance()));
            addRow(sheet, rowIdx++, "Thuế TNCN", format(payroll.getPersonalIncomeTax()));
            addRow(sheet, rowIdx++, "Tổng khấu trừ", format(payroll.getTotalDeduction()));
            addRow(sheet, rowIdx++, "Thực lĩnh", format(payroll.getNetSalary()));
            addRow(sheet, rowIdx++, "Trạng thái", payroll.getStatus().name());

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Không thể xuất file Excel", e);
        }
    }

    @Override
    public byte[] exportToPdf(Long payrollId) {
        Payroll payroll = findPayrollInTenant(payrollId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("PHIẾU LƯƠNG - " + payroll.getSalaryMonth(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addPdfRow(table, "Nhân viên", payroll.getEmployee().getFullName());
            addPdfRow(table, "Mã NV", payroll.getEmployee().getEmployeeCode());
            addPdfRow(table, "Lương cơ bản", format(payroll.getBaseSalary()));
            addPdfRow(table, "Phụ cấp", format(payroll.getAllowance()));
            addPdfRow(table, "Tổng thu nhập", format(payroll.getGrossIncome()));
            addPdfRow(table, "BHXH", format(payroll.getSocialInsurance()));
            addPdfRow(table, "BHYT", format(payroll.getHealthInsurance()));
            addPdfRow(table, "BHTN", format(payroll.getUnemploymentInsurance()));
            addPdfRow(table, "Thuế TNCN", format(payroll.getPersonalIncomeTax()));
            addPdfRow(table, "Tổng khấu trừ", format(payroll.getTotalDeduction()));
            addPdfRow(table, "Thực lĩnh", format(payroll.getNetSalary()));
            addPdfRow(table, "Trạng thái", payroll.getStatus().name());
            document.add(table);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Không thể xuất file PDF", e);
        }
    }

    private void addRow(Sheet sheet, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void addPdfRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        table.addCell(labelCell);
        table.addCell(new Phrase(value != null ? value : ""));
    }

    private String format(BigDecimal amount) {
        if (amount == null) return "0";
        return currencyFormat.format(amount) + " VND";
    }

    private Payroll findPayrollInTenant(Long id) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new BadRequestException("Không xác định được công ty");
        }
        return payrollRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng lương"));
    }
}
