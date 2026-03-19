package com.abanoj.scheman.employee.service;

import com.abanoj.scheman.auth.dto.RegisterRequest;
import com.abanoj.scheman.employee.dto.EmployeeResponse;

public interface EmployeeService {
    EmployeeResponse create(RegisterRequest request);
}
