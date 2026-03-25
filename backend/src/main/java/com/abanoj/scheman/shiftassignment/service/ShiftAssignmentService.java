package com.abanoj.scheman.shiftassignment.service;

import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShiftAssignmentService {
    Page<ShiftAssignmentResponseDto> findAllShiftsAssignmentsByShiftId(Pageable pageable, UUID shiftId);
    Page<ShiftAssignmentResponseDto> findAllShiftsAssignmentsByEmployeeId(Pageable pageable, UUID employeeId);
    ShiftAssignmentResponseDto findShiftAssignmentById(UUID id);
    ShiftAssignmentResponseDto createShiftAssignment(UUID shiftId, ShiftAssignmentCreateRequestDto shiftAssignment);
    ShiftAssignmentResponseDto updateShiftAssignment(UUID shiftId, UUID id, ShiftAssignmentUpdateRequestDto shiftAssignment);
    void delete(UUID shiftId, UUID id);
}
