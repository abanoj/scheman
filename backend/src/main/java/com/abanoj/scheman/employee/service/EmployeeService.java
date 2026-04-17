package com.abanoj.scheman.employee.service;

import com.abanoj.scheman.employee.dto.EmployeeCreateRequestDto;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {
    Page<EmployeeResponseDto> findAllEmployees(Pageable pageable);
    EmployeeResponseDto create(EmployeeCreateRequestDto request);
    EmployeeResponseDto updateEmployee(UUID employeeId, EmployeeUpdateRequestDto employeeUpdateRequestDto);
    EmployeeResponseDto findEmployeeById(UUID employeeId);
    void disableEmployeeById(UUID employeeId);
    void enableEmployeeById(UUID employeeId);
}
