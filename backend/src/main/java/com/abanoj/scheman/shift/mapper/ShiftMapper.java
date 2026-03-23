package com.abanoj.scheman.shift.mapper;

import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    @Mapping(source = "store.id", target = "storeId")
    ShiftResponseDto toResponseDto(Shift shift);
    @Mapping(target = "store", ignore = true )
    Shift toShift(ShiftCreateRequestDto shiftCreateRequestDto);
    @Mapping(target = "store", ignore = true )
    Shift toShift(ShiftUpdateRequestDto shiftUpdateRequestDto);
}
