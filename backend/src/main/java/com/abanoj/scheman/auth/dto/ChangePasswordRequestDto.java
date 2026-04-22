package com.abanoj.scheman.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String oldPassword,
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword,
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String repeatNewPassword
) {
    @AssertTrue(message = "Passwords must match")
    public boolean isPasswordMatching(){
        return newPassword != null && newPassword.equals(repeatNewPassword);
    }
}
