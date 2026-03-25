package com.abanoj.scheman.store.controller;

import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    @Operation(summary = "Get all stores")
    public ResponseEntity<Page<StoreResponseDto>> getAllStore(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(storeService.findAllStore(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get a Store by ID")
    public ResponseEntity<StoreResponseDto> getStoreById(@Parameter(description = "Store ID") UUID id){
        return ResponseEntity.ok(storeService.findStoreById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new Store")
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody StoreCreateRequestDto storeCreateRequestDto){
        return ResponseEntity.ok(storeService.createStore(storeCreateRequestDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing Store")
    public ResponseEntity<StoreResponseDto> updateStore(
            @Parameter(description = "Store ID") UUID id,
            @Valid @RequestBody StoreUpdateRequestDto storeUpdateRequestDto){
        return ResponseEntity.ok(storeService.updateStore(id, storeUpdateRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a Store by ID")
    public ResponseEntity<Void> deleteStore(@Parameter(description = "Store ID") UUID id){
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
