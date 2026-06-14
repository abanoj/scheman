package com.abanoj.scheman.auth.service;

import com.abanoj.scheman.auth.dto.ManagerResponseDto;
import com.abanoj.scheman.auth.dto.ManagerUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ManagerService {
    Page<ManagerResponseDto> findAll(Pageable pageable);
    ManagerResponseDto findById(UUID id);
    ManagerResponseDto update(UUID id, ManagerUpdateRequestDto dto);
    void enable(UUID id);
    void disable(UUID id);
}
