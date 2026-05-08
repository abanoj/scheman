package com.abanoj.scheman.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record EmployeeSummaryResponseDto(
        @Schema(description = "Employee ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Employee name", example = "Jesus Abano")
        String name
) {
}
