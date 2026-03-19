package com.abanoj.scheman.employee.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EmployeeResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
