package com.abanoj.scheman.auth.controller;

import com.abanoj.scheman.auth.dto.*;
import com.abanoj.scheman.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return tokens")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup/manager")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user manager")
    public ResponseEntity<ManagerResponseDto> createManager(@Valid @RequestBody ManagerCreateRequestDto managerCreateRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createManager(managerCreateRequestDto));
    }

    @PatchMapping("/{userId}/password")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(summary = "Change the current password")
    public ResponseEntity<Void> changePassword(@Parameter(description = "User id") @PathVariable UUID userId,
                                               @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto){
        authService.changePassword(userId, changePasswordRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
