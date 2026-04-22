package com.abanoj.scheman.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ManagerResponseDto(
        @Schema(description = "User ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID id,
        @Schema(description = "First name", example = "John")
        String firstName,
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        @Schema(description = "Email address", example = "john@doe.com")
        String email) {
}
