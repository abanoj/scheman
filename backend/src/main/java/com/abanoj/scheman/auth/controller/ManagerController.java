package com.abanoj.scheman.auth.controller;

import com.abanoj.scheman.auth.dto.ManagerResponseDto;
import com.abanoj.scheman.auth.dto.ManagerUpdateRequestDto;
import com.abanoj.scheman.auth.service.ManagerService;
import com.abanoj.scheman.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Managers", description = "Manager account management endpoints")
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping
    @Operation(summary = "List all managers (paginated)", responses = {
            @ApiResponse(responseCode = "200", description = "List returned successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ManagerResponseDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(managerService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a manager by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Manager found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ManagerResponseDto> findById(@Parameter(description = "Manager user ID") @PathVariable UUID id) {
        return ResponseEntity.ok(managerService.findById(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update manager name", responses = {
            @ApiResponse(responseCode = "200", description = "Manager updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ManagerResponseDto> update(
            @Parameter(description = "Manager user ID") @PathVariable UUID id,
            @Valid @RequestBody ManagerUpdateRequestDto dto) {
        return ResponseEntity.ok(managerService.update(id, dto));
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Enable a manager account", responses = {
            @ApiResponse(responseCode = "204", description = "Manager enabled"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> enable(@Parameter(description = "Manager user ID") @PathVariable UUID id) {
        managerService.enable(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable a manager account", responses = {
            @ApiResponse(responseCode = "204", description = "Manager disabled"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> disable(@Parameter(description = "Manager user ID") @PathVariable UUID id) {
        managerService.disable(id);
        return ResponseEntity.noContent().build();
    }
}
