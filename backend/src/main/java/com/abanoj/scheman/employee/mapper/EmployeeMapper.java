package com.abanoj.scheman.employee.mapper;

import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeSummaryResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;
import com.abanoj.scheman.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "employee.id", target = "id")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.enabled", target = "enabled")
    EmployeeResponseDto toResponseUserEmployeeDto(Employee employee, User user);

    @Mapping(target = "name",
            expression = "java(employee.getUser().getFirstName() + ' ' + employee.getUser().getLastName())")
    EmployeeSummaryResponseDto toEmployeeSummaryDto(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "preferredStores", ignore = true)
    void updateEmployeeFromDto(EmployeeUpdateRequestDto employeeUpdateRequestDto, @MappingTarget Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(EmployeeUpdateRequestDto employeeUpdateRequestDto, @MappingTarget User user);

}
