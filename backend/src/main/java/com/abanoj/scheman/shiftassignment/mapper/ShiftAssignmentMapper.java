package com.abanoj.scheman.shiftassignment.mapper;

import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShiftAssignmentMapper {
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "shift.id", target = "shiftId")
    ShiftAssignmentResponseDto toResponseDto(ShiftAssignment shiftAssignment);
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shift", ignore = true)
    ShiftAssignment toShiftAssignment(ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto);
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shift", ignore = true)
    ShiftAssignment toShiftAssignment(ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto);
}
