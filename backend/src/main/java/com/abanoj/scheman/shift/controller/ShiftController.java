package com.abanoj.scheman.shift.controller;

import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.service.ShiftService;
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
@RequestMapping("/api/v1/stores/{storeId}/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Shift management endpoints")
public class ShiftController {
    private final ShiftService shiftService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all shifts for a store")
    public ResponseEntity<Page<ShiftResponseDto>> getAllShiftsByStoreId(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Store ID") @PathVariable UUID storeId) {
        return ResponseEntity.ok(shiftService.findAllShiftsByStoreId(pageable, storeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get a shift by ID")
    public ResponseEntity<ShiftResponseDto> getShiftById(
            @Parameter(description = "Store ID") @PathVariable UUID storeId,
            @Parameter(description = "Shift ID") @PathVariable UUID id) {
        return ResponseEntity.ok(shiftService.findShiftById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new shift")
    public ResponseEntity<ShiftResponseDto> createShift(
            @Parameter(description = "Store ID") @PathVariable UUID storeId,
            @Valid @RequestBody ShiftCreateRequestDto shiftCreateRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.createShift(storeId, shiftCreateRequestDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing shift")
    public ResponseEntity<ShiftResponseDto> updateShift(
            @Parameter(description = "Store ID") @PathVariable UUID storeId,
            @Parameter(description = "Shift ID") @PathVariable UUID id,
            @Valid @RequestBody ShiftUpdateRequestDto shiftUpdateRequestDto) {
        return ResponseEntity.ok(shiftService.updateShift(storeId, id, shiftUpdateRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a shift by ID")
    public ResponseEntity<Void> deleteShift(
            @Parameter(description = "Store ID") @PathVariable UUID storeId,
            @Parameter(description = "Shift ID") @PathVariable UUID id) {
        shiftService.delete(storeId, id);
        return ResponseEntity.noContent().build();
    }
}
