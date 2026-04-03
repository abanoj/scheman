package com.abanoj.scheman.shiftassignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ShiftAssignmentUpdateRequestDto(
        @Schema(description = "Shift assignment date", example = "2026-03-23")
        @NotNull(message = "There must be a date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @Schema(description = "Employee ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        @NotNull(message = "There must be an employee ID")
        UUID employeeId,
        @Schema(description = "Shift ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        @NotNull(message = "There must be a shift ID")
        UUID shiftId
) {
}
