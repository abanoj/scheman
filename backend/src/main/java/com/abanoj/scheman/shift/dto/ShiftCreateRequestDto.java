package com.abanoj.scheman.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftCreateRequestDto(
        @Schema(description = "Shift name", example = "Tarde")
        @NotBlank(message = "There must be a shift name")
        String name,
        @Schema(description = "Store ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        @NotNull(message = "There must be a store ID")
        UUID storeId,
        @Schema(description = "Shift start time", example = "15:30:00")
        @NotNull(message = "There must be a start time")
        LocalTime startTime,
        @Schema(description = "Shift end time", example = "23:30:00")
        @NotNull(message = "There must be an end time")
        LocalTime endTime
) {
}
