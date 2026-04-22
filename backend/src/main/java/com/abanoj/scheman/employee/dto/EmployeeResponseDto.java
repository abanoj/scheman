package com.abanoj.scheman.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record EmployeeResponseDto(
        @Schema(description = "Employee ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "Employee DNI or NIE", example = "Y8479910J")
        String dni,
        @Schema(description = "First name", example = "John")
        String firstName,
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        @Schema(description = "Email address", example = "john@doe.com")
        String email,
        @Schema(description = "Store ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID userId,
        @Schema(description = "Weekly contracted hours", example = "40")
        Integer weeklyContractedHours
) {
}
