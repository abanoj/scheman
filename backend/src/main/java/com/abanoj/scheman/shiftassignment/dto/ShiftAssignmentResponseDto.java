package com.abanoj.scheman.shiftassignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ShiftAssignmentResponseDto(
        @Schema(description = "Shift ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Shift assignment date", example = "2026-03-23")
        LocalDate date,
        @Schema(description = "Employee ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID employeeId,
        @Schema(description = "Shift ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID shiftId
) {
}
