package com.abanoj.scheman.employee.controller;

import com.abanoj.scheman.employee.dto.EmployeeCreateRequestDto;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;
import com.abanoj.scheman.employee.service.EmployeeService;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ShiftAssignmentService shiftAssignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all employees")
    public ResponseEntity<Page<EmployeeResponseDto>> findAllEmployees(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(employeeService.findAllEmployees(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Register a new employee")
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get an Employee by Id")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@Parameter(description = "Employee id") @PathVariable UUID employeeId){
        return ResponseEntity.ok(employeeService.findEmployeeById(employeeId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get employee information")
    public ResponseEntity<EmployeeResponseDto> getMyProfile(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(employeeService.findEmployeeById(user.getId()));
    }

    @PatchMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #employeeId == authentication.principal.id")
    @Operation(summary = "Update employee information")
    public ResponseEntity<EmployeeResponseDto> updateEmployeeById(@Parameter(description = "Employee id") @PathVariable UUID employeeId,
                                                                  @Valid @RequestBody EmployeeUpdateRequestDto employeeUpdateRequestDto){
        return ResponseEntity.ok(employeeService.updateEmployee(employeeId, employeeUpdateRequestDto));
    }

    @GetMapping("/{employeeId}/shift-assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #employeeId == authentication.principal.id")
    @Operation(summary = "Get all shift assignments for an employee")
    public ResponseEntity<Page<ShiftAssignmentResponseDto>> getShiftAssignmentsByEmployeeId(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId) {
        return ResponseEntity.ok(shiftAssignmentService.findAllShiftsAssignmentsByEmployeeId(pageable, employeeId));
    }

    @PatchMapping("/{employeeId}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Disable an employee account")
    public ResponseEntity<Void> disableEmployee(@Parameter(description = "Employee id") @PathVariable UUID employeeId){
        employeeService.disableEmployeeById(employeeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{employeeId}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Enable an employee account")
    public ResponseEntity<Void> enableEmployee(@Parameter(description = "Employee id") @PathVariable UUID employeeId){
        employeeService.enableEmployeeById(employeeId);
        return ResponseEntity.noContent().build();
    }
}
