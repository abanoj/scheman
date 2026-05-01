package com.abanoj.scheman.employee.dto;

import com.abanoj.scheman.shift.entity.ShiftType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EmployeeUpdateRequestDto(
        @Schema(description = "Employee DNI or NIE", example = "Y8479910J")
        @Size(min = 9, max = 9, message = "DNI must be exactly 9 characters")
        String dni,
        @Schema(description = "First name", example = "John")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,
        @Schema(description = "Last name", example = "Doe")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,
        @Schema(description = "Email address", example = "john@doe.com")
        @Email(message = "Invalid email format")
        String email,
        @Schema(description = "Preferred shift", example = "MORNING, AFTERNOON or NIGHT")
        @NotBlank(message = "Preferred shift is required")
        ShiftType preferredShift,
        @Schema(description = "Weekly contracted hours", example = "40")
        @Positive(message = "Weekly contracted hours must be positive")
        Integer weeklyContractedHours
) {
}
