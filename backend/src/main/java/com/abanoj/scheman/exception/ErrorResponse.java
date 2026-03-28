package com.abanoj.scheman.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Integer status,
        String error,
        String message,
        Instant timestamp,
        String path,
        Map<String, String> validationErrors
) {
}
