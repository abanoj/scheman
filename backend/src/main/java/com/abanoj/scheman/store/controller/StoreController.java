package com.abanoj.scheman.store.controller;

import com.abanoj.scheman.exception.ErrorResponse;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreListResponseDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.service.StoreService;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Stores", description = "Store management endpoints")
public class StoreController {
    private final StoreService storeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all stores", responses = {
            @ApiResponse(responseCode = "200", description = "Stores retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<StoreListResponseDto>> getAllStores(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(storeService.findAllStores(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get a Store by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Store retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Store not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StoreResponseDto> getStoreById(@Parameter(description = "Store ID") @PathVariable UUID id){
        return ResponseEntity.ok(storeService.findStoreById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new Store", responses = {
            @ApiResponse(responseCode = "201", description = "Store created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Store already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody StoreCreateRequestDto storeCreateRequestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(storeCreateRequestDto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Partially update an existing Store", responses = {
            @ApiResponse(responseCode = "200", description = "Store updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Store not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StoreResponseDto> updateStore(
            @Parameter(description = "Store ID") @PathVariable UUID id,
            @Valid @RequestBody StoreUpdateRequestDto storeUpdateRequestDto){
        return ResponseEntity.ok(storeService.updateStore(id, storeUpdateRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a Store by ID", responses = {
            @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Store not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Store has associated shifts",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteStore(@Parameter(description = "Store ID") @PathVariable UUID id){
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
