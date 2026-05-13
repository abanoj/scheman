package com.abanoj.scheman.shift.dto;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

public record UnassignedShiftResponseDto(
        UUID shiftId,
        String shiftName,
        Set<DayOfWeek> unassignedDays
) {
}
