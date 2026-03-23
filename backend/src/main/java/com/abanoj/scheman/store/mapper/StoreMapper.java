package com.abanoj.scheman.store.mapper;

import com.abanoj.scheman.shift.mapper.ShiftMapper;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.entity.Store;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ShiftMapper.class})
public interface StoreMapper {
    StoreResponseDto toResponseDto(Store store);
    Store toStore(StoreCreateRequestDto storeCreateRequestDto);
    Store toStore(StoreUpdateRequestDto storeUpdateRequestDto);
}
