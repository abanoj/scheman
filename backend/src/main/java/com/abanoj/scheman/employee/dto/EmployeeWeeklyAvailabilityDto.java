package com.abanoj.scheman.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record EmployeeWeeklyAvailabilityDto(
        @Schema(description = "Employee ID")
        UUID id,
        @Schema(description = "First name")
        String firstName,
        @Schema(description = "Last name")
        String lastName,
        @Schema(description = "Weekly contracted hours")
        Integer weeklyContractedHours,
        @Schema(description = "Hours already assigned this week")
        double assignedHours,
        @Schema(description = "Available hours remaining this week")
        double availableHours
) {
}
