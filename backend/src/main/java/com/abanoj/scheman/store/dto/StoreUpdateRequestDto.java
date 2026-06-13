package com.abanoj.scheman.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record StoreUpdateRequestDto(
        @Schema(description = "Store title", example = "San Blas")
        String name,
        @Schema(description = "Store address", example = "Plaza General Mancha, N2")
        String address,
        @Schema(description = "Store phone number", example = "965910223")
        @Pattern(regexp = "^([6-9][0-9]{8})?$", message = "Must be a valid Spanish phone number")
        String phone,
        @Schema(description = "Is the store open 24 hours?", example = "true")
        Boolean is24h
) {
}
