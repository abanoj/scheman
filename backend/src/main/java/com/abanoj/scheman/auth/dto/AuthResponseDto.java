package com.abanoj.scheman.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDto(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("must_change_password")
        boolean mustChangePassword
) {
}
