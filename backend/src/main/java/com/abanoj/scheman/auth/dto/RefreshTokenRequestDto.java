package com.abanoj.scheman.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
