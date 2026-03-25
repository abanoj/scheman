package com.abanoj.scheman.shift.service;

import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShiftService {
    Page<ShiftResponseDto> findAllShiftsByStoreId(Pageable pageable, UUID storeId);
    ShiftResponseDto findShiftById(UUID id);
    ShiftResponseDto createShift(UUID storeId, ShiftCreateRequestDto shift);
    ShiftResponseDto updateShift(UUID storeId, UUID id, ShiftUpdateRequestDto shift);
    void delete(UUID storeId, UUID id);
}
