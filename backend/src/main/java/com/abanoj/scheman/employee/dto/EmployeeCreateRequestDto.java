package com.abanoj.scheman.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EmployeeCreateRequestDto(
        @NotBlank(message = "DNI is required")
        @Size(min = 9, max = 9, message = "DNI must be exactly 9 characters")
        String dni,
        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        @Schema(description = "Weekly contracted hours", example = "40")
        @Positive(message = "Weekly contracted hours must be positive")
        Integer weeklyContractedHours
) {
}
