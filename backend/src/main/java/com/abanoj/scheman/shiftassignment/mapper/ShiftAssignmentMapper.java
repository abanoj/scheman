package com.abanoj.scheman.shiftassignment.mapper;

import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.WeeklyAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShiftAssignmentMapper {
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "shift.id", target = "shiftId")
    ShiftAssignmentResponseDto toResponseDto(ShiftAssignment shiftAssignment);

    @Mapping(source = "shift.id", target = "shiftId")
    @Mapping(source = "shift.name", target = "shiftName")
    @Mapping(source = "shift.startTime", target = "startTime")
    @Mapping(source = "shift.endTime", target = "endTime")
    @Mapping(source = "shift.shiftType", target = "shiftType")
    @Mapping(source = "shift.crossesMidnight", target = "crossesMidnight")
    @Mapping(source = "shift.totalHours", target = "totalHours")
    @Mapping(source = "shift.store.id", target = "storeId")
    @Mapping(source = "shift.store.name", target = "storeName")
    WeeklyAssignmentResponseDto toWeeklyResponseDto(ShiftAssignment shiftAssignment);
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
