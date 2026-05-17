package com.abanoj.scheman.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Password is required")
        String oldPassword,
        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
                message = "Password must be at least 8 characters and contain one uppercase, one lowercase, one digit and one special character"
        )
        String newPassword,
        @NotBlank(message = "New password is required")
        String repeatNewPassword
) {
    @AssertTrue(message = "Passwords must match")
    public boolean isPasswordMatching(){
        return newPassword != null && newPassword.equals(repeatNewPassword);
    }
}
