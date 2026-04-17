package com.abanoj.scheman.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftCreateRequestDto(
        @Schema(description = "Shift name", example = "Night")
        @NotBlank(message = "There must be a shift name")
        String name,
        @Schema(description = "Shift start time", example = "15:30:00")
        @NotNull(message = "There must be a start time")
        LocalTime startTime,
        @Schema(description = "Shift end time", example = "23:30:00")
        @NotNull(message = "There must be an end time")
        LocalTime endTime
) {
}
