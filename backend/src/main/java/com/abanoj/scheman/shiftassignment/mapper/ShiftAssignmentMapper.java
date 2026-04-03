package com.abanoj.scheman.shiftassignment.mapper;

import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShiftAssignmentMapper {
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "shift.id", target = "shiftId")
    ShiftAssignmentResponseDto toResponseDto(ShiftAssignment shiftAssignment);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ShiftAssignment toShiftAssignment(ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateShiftAssignmentFromDto(ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto, @MappingTarget ShiftAssignment shiftAssignment);
}
