package com.abanoj.scheman.shiftassignment.service;

import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShiftAssignmentService {
    Page<ShiftAssignmentResponseDto> findAllShiftsAssignmentsByShiftId(Pageable pageable, UUID shiftId);
    ShiftAssignmentResponseDto findShiftAssignmentById(UUID shiftId, UUID id);
    ShiftAssignmentResponseDto createShiftAssignment(UUID shiftId, ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto);
    ShiftAssignmentResponseDto updateShiftAssignment(UUID shiftId, UUID id, ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto);
    void delete(UUID shiftId, UUID id);
}
