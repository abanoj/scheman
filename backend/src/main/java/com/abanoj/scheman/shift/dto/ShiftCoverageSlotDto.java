package com.abanoj.scheman.shift.dto;

import com.abanoj.scheman.shift.entity.ShiftType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ShiftCoverageSlotDto(
        @Schema(description = "Date of the slot", example = "2026-06-10")
        LocalDate date,
        @Schema(description = "Day of the week", example = "WEDNESDAY")
        DayOfWeek dayOfWeek,
        @Schema(description = "Shift ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID shiftId,
        @Schema(description = "Shift name", example = "Morning")
        String shiftName,
        @Schema(description = "Type of shift", example = "MORNING")
        ShiftType shiftType,
        @Schema(description = "Shift start time", example = "07:00:00")
        LocalTime startTime,
        @Schema(description = "Shift end time", example = "15:00:00")
        LocalTime endTime,
        @Schema(description = "Does the shift cross midnight?", example = "false")
        boolean crossesMidnight,
        @Schema(description = "Whether the slot is assigned to an employee", example = "true")
        boolean assigned,
        @Schema(description = "Assignment ID (null when unassigned)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID assignmentId,
        @Schema(description = "Assigned employee ID (null when unassigned)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID employeeId,
        @Schema(description = "Assigned employee full name (null when unassigned)", example = "María García")
        String employeeName
) {
}
