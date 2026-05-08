package com.abanoj.scheman.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record StoreSummaryResponseDto(
        @Schema(description = "Store ID", example = "1")
        UUID id,
        @Schema(description = "Store title", example = "San Blas")
        String name
) {
}
