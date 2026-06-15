package com.abanoj.scheman.shiftassignment.controller;

import com.abanoj.scheman.employee.dto.EmployeeWeeklyAvailabilityDto;
import com.abanoj.scheman.exception.ErrorResponse;
import com.abanoj.scheman.shiftassignment.dto.WeeklyAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shift-assignments/employees")
@RequiredArgsConstructor
@Tag(name = "Shift Assignments", description = "Employees schedule queries")
public class EmployeeShiftAssignmentController {
    private final ShiftAssignmentService shiftAssignmentService;

    @GetMapping("/weekly-availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get weekly availability for all enabled employees",
            description = "Returns employees with available hours for the week containing the given date, sorted by available hours descending",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Weekly availability retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<List<EmployeeWeeklyAvailabilityDto>> getWeeklyAvailability(
            @Parameter(description = "Any date within the desired week (yyyy-MM-dd)") @RequestParam LocalDate date) {
        return ResponseEntity.ok(shiftAssignmentService.findWeeklyAvailability(date));
    }

    @GetMapping("/{employeeId}/weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #employeeId == authentication.principal.id")
    @Operation(summary = "Get weekly shift assignments for an employee",
            description = "Returns all shift assignments for the week (Monday to Sunday) containing the given date",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Weekly shift assignments retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Employee not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    public ResponseEntity<List<WeeklyAssignmentResponseDto>> getWeeklyShiftAssignmentsByEmployeeAndDates(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId,
            @Parameter(description = "Any date within the desired week (yyyy-MM-dd)") @RequestParam LocalDate date){
        return ResponseEntity.ok(shiftAssignmentService.findWeeklyAssignmentsByEmployee(employeeId, date));
    }
}
