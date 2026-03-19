package com.abanoj.scheman.employee.mapper;

import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.employee.dto.EmployeeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeResponse toResponse(User user);
}
