package com.abanoj.scheman.shift.dto;

import com.abanoj.scheman.shift.entity.ShiftType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record ShiftUpdateRequestDto(
        @Schema(description = "Shift name", example = "San Blas Night")
        @NotBlank(message = "There must be a shift name")
        String name,
        @Schema(description = "Shift start time", example = "15:30:00")
        @NotNull(message = "There must be a start time")
        LocalTime startTime,
        @Schema(description = "Shift end time", example = "23:30:00")
        @NotNull(message = "There must be an end time")
        LocalTime endTime,
        @Schema(description = "Type of shift", example = "MORNING, AFTERNOON or NIGHT")
        @NotNull(message = "There must be an shift type")
        ShiftType shiftType,
        @Schema(description = "List of available days", example = "MONDAY, WEDNESDAY, FRIDAY")
        @NotEmpty(message = "There must be at least one available day")
        Set<DayOfWeek> availableDays
) {
}
