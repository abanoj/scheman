package com.abanoj.scheman.employee.mapper;

import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;
import com.abanoj.scheman.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeResponseDto toResponseDto(Employee employee);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "employee.id", target = "id")
    EmployeeResponseDto toResponseUserEmployeeDto(Employee employee, User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeFromDto(EmployeeUpdateRequestDto employeeUpdateRequestDto, @MappingTarget Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(EmployeeUpdateRequestDto employeeUpdateRequestDto, @MappingTarget User user);
}
