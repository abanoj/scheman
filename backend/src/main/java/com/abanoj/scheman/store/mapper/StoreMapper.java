package com.abanoj.scheman.store.mapper;

import com.abanoj.scheman.shift.mapper.ShiftMapper;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.entity.Store;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ShiftMapper.class})
public interface StoreMapper {
    StoreResponseDto toResponseDto(Store store);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    Store toStore(StoreCreateRequestDto storeCreateRequestDto);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateStoreFromDto(StoreUpdateRequestDto storeUpdateRequestDto, @MappingTarget Store store);
}
