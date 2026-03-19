package com.abanoj.scheman.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record StoreUpdateRequest(
        @Schema(description = "Store ID", example = "1")
        @NotNull(message = "There must be an ID")
        UUID id,
        @Schema(description = "Store title", example = "San Blas")
        @NotBlank(message = "There must be a store name")
        String name,
        @Schema(description = "Store address", example = "Plaza General Mancha, N2")
        String address,
        @Schema(description = "Store phone number", example = "965910223")
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Must be a valid phone number")
        Integer phone,
        @Schema(description = "Is the store open 24 hours?", example = "true")
        Boolean is24h
) {
}
