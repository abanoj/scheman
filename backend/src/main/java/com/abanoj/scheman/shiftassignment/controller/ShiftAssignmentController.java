package com.abanoj.scheman.shiftassignment.controller;

import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
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
    @Operation(summary = "Get all shift assignments for a shift")
    public ResponseEntity<Page<ShiftAssignmentResponseDto>> getAllShiftAssignmentsByShiftId(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId) {
        return ResponseEntity.ok(shiftAssignmentService.findAllShiftsAssignmentsByShiftId(pageable, shiftId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get a shift assignment by ID")
    public ResponseEntity<ShiftAssignmentResponseDto> getShiftAssignmentById(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Parameter(description = "Shift Assignment ID") @PathVariable UUID id) {
        return ResponseEntity.ok(shiftAssignmentService.findShiftAssignmentById(shiftId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new shift assignment")
    public ResponseEntity<ShiftAssignmentResponseDto> createShiftAssignment(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Valid @RequestBody ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftAssignmentService.createShiftAssignment(shiftId, shiftAssignmentCreateRequestDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing shift assignment")
    public ResponseEntity<ShiftAssignmentResponseDto> updateShiftAssignment(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Parameter(description = "Shift Assignment ID") @PathVariable UUID id,
            @Valid @RequestBody ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto) {
        return ResponseEntity.ok(shiftAssignmentService.updateShiftAssignment(shiftId, id, shiftAssignmentUpdateRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a shift assignment by ID")
    public ResponseEntity<Void> deleteShiftAssignment(
            @Parameter(description = "Shift ID") @PathVariable UUID shiftId,
            @Parameter(description = "Shift Assignment ID") @PathVariable UUID id) {
        shiftAssignmentService.delete(shiftId, id);
        return ResponseEntity.noContent().build();
    }
}
