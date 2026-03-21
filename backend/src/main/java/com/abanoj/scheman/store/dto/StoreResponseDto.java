package com.abanoj.scheman.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record StoreResponseDto(
        @Schema(description = "Store ID", example = "1")
        UUID id,
        @Schema(description = "Store title", example = "San Blas")
        String name,
        @Schema(description = "Store address", example = "Plaza General Mancha, N2")
        String address,
        @Schema(description = "Store phone number", example = "965910223")
        Integer phone,
        @Schema(description = "Is the store open 24 hours?", example = "true")
        Boolean is24h
) {
}
