package com.abanoj.scheman.employee.service;

import com.abanoj.scheman.auth.dto.RegisterRequest;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;
import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.mapper.EmployeeMapper;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponseDto create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .build();

        userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .dni(request.dni())
                .weeklyContractedHours(request.weeklyContractedHours())
                .build();

        employeeRepository.save(employee);
        log.info("New employee created: {} {}", request.firstName(), request.lastName());
        return employeeMapper.toResponseDto(employee, user);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updatedEmployee(UUID employeeId, EmployeeUpdateRequestDto employeeUpdateRequestDto) {
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Not found employee with id: " + employeeId));
        User user = userRepository
                .findById(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Not found user with id: " + employeeId));
        employeeMapper.updateEmployeeFromDto(employeeUpdateRequestDto, employee);
        employeeRepository.save(employee);
        log.info("Updated employee with id {}", employeeId);
        return employeeMapper.toResponseDto(employee, user);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto findEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(()-> new ResourceNotFoundException("Not found employee with employeeId " + employeeId));
        User user = userRepository.findById(employee.getUser().getId()).orElseThrow(()-> new ResourceNotFoundException("Not found user with employeeId " + employee.getUser().getId()));
        return employeeMapper.toResponseDto(employee, user);
    }
}
