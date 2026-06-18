package com.nexahr.service;

import com.nexahr.dto.response.PageResponse;
import com.nexahr.dto.response.PayrollResponse;
import com.nexahr.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PayrollService {
    PageResponse<PayrollResponse> getAll(String month, Pageable pageable);
    PageResponse<PayrollResponse> getMy(Long employeeId, Pageable pageable);
    PayrollResponse getById(Long id);
    PayrollResponse getByIdForUser(Long id, User user);
    List<PayrollResponse> generate(String month);
    PayrollResponse approve(Long id);
    PayrollResponse markPaid(Long id);
}
