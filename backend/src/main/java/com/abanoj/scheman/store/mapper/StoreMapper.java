package com.abanoj.scheman.store.mapper;

import com.abanoj.scheman.employee.mapper.EmployeeMapper;
import com.abanoj.scheman.shift.mapper.ShiftMapper;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreListResponseDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreSummaryResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.entity.Store;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ShiftMapper.class, EmployeeMapper.class})
public interface StoreMapper {
    StoreResponseDto toResponseDto(Store store);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    @Mapping(target = "preferredEmployees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Store toStore(StoreCreateRequestDto storeCreateRequestDto);

    StoreListResponseDto toListResponseDto(Store store);

    StoreSummaryResponseDto toStoreSummaryDto(Store store);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shifts", ignore = true)
    @Mapping(target = "preferredEmployees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateStoreFromDto(StoreUpdateRequestDto storeUpdateRequestDto, @MappingTarget Store store);
}
