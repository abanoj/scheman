package com.abanoj.scheman.shiftassignment.dto;

import com.abanoj.scheman.shift.entity.ShiftType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record WeeklyAssignmentResponseDto(
        @Schema(description = "Shift Assignment ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Assignment date", example = "2026-03-23")
        LocalDate date,
        @Schema(description = "Shift ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID shiftId,
        @Schema(description = "Shift name", example = "Morning")
        String shiftName,
        @Schema(description = "Shift start time", example = "07:00:00")
        LocalTime startTime,
        @Schema(description = "Shift end time", example = "15:00:00")
        LocalTime endTime,
        @Schema(description = "Type of shift", example = "MORNING")
        ShiftType shiftType,
        @Schema(description = "Does the shift cross midnight?", example = "false")
        boolean crossesMidnight,
        @Schema(description = "Total shift hours", example = "8.0")
        double totalHours,
        @Schema(description = "Store ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID storeId,
        @Schema(description = "Store name", example = "San Blas")
        String storeName
) {
}
