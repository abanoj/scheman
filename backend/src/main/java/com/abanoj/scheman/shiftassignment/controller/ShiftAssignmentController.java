package com.abanoj.scheman.shiftassignment.controller;

import com.abanoj.scheman.exception.ErrorResponse;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts/{shiftId}/shift-assignments")
@RequiredArgsConstructor
@Tag(name = "Shift Assignments", description = "Shift assignment management endpoints")
public class ShiftAssignmentController {
    private final ShiftAssignmentService shiftAssignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all shift assignments for a shift", responses = {
            @ApiResponse(responseCode = "200", description = "Shift assignments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ShiftAssignmentResponseDto>> getAllShiftAssignmentsByShiftId(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId) {
        return ResponseEntity.ok(shiftAssignmentService.findAllShiftsAssignmentsByShiftId(pageable, shiftId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #employeeId == authentication.principal.id")
    @Operation(summary = "Get all shift assignments for an employee", responses = {
            @ApiResponse(responseCode = "200", description = "Shift assignments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ShiftAssignmentResponseDto>> getShiftAssignmentsByEmployeeId(
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId) {
        return ResponseEntity.ok(shiftAssignmentService.findAllShiftsAssignmentsByEmployeeId(pageable, employeeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get a shift assignment by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Shift assignment retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shift assignment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShiftAssignmentResponseDto> getShiftAssignmentById(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Parameter(description = "Shift Assignment ID") @PathVariable UUID id) {
        return ResponseEntity.ok(shiftAssignmentService.findShiftAssignmentById(shiftId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new shift assignment", responses = {
            @ApiResponse(responseCode = "201", description = "Shift assignment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shift or employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Assignment overlaps with an existing one",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShiftAssignmentResponseDto> createShiftAssignment(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Valid @RequestBody ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftAssignmentService.createShiftAssignment(shiftId, shiftAssignmentCreateRequestDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing shift assignment", responses = {
            @ApiResponse(responseCode = "200", description = "Shift assignment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shift assignment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Assignment overlaps with an existing one",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShiftAssignmentResponseDto> updateShiftAssignment(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Parameter(description = "Shift Assignment ID") @PathVariable UUID id,
            @Valid @RequestBody ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto) {
        return ResponseEntity.ok(shiftAssignmentService.updateShiftAssignment(shiftId, id, shiftAssignmentUpdateRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a shift assignment by ID", responses = {
            @ApiResponse(responseCode = "204", description = "Shift assignment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Shift assignment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteShiftAssignment(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Parameter(description = "Shift Assignment ID") @PathVariable UUID id) {
        shiftAssignmentService.delete(shiftId, id);
        return ResponseEntity.noContent().build();
    }
}
