package com.abanoj.scheman.shift.mapper;

import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    @Mapping(source = "store.id", target = "storeId")
    ShiftResponseDto toResponseDto(Shift shift);
    @Mapping(target = "id", ignore = true )
    @Mapping(target = "store", ignore = true )
    Shift toShift(ShiftCreateRequestDto shiftCreateRequestDto);
    @Mapping(target = "id", ignore = true )
    @Mapping(target = "createdAt", ignore = true )
    @Mapping(target = "updatedAt", ignore = true )
    @Mapping(target = "store", ignore = true )
    void updateShiftFromDto(ShiftUpdateRequestDto shiftUpdateRequestDto, @MappingTarget Shift shift);
}
