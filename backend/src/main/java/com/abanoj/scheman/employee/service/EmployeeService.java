package com.abanoj.scheman.employee.service;

import com.abanoj.scheman.auth.dto.RegisterRequest;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;

import java.util.UUID;

public interface EmployeeService {
    EmployeeResponseDto create(RegisterRequest request);
    EmployeeResponseDto updatedEmployee(UUID employeeId, EmployeeUpdateRequestDto employeeUpdateRequestDto);
    EmployeeResponseDto findEmployeeById(UUID employeeId);
}
