package com.abanoj.scheman.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftResponseDto(
        @Schema(description = "Shift ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Shift name", example = "Afternoon")
        String name,
        @Schema(description = "Store ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID storeId,
        @Schema(description = "Shift start time", example = "15:30:00")
        LocalTime startTime,
        @Schema(description = "Shift end time", example = "23:30:00")
        LocalTime endTime,
        @Schema(description = "Is shift crossing midnight?", example = "false")
        boolean crossesMidnight
) {
}
