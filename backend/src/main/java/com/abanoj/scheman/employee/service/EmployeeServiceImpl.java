package com.abanoj.scheman.employee.service;

import com.abanoj.scheman.auth.dto.RegisterRequest;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.employee.dto.EmployeeResponse;
import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.mapper.EmployeeMapper;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponse create(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
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
                .build();

        employeeRepository.save(employee);

        return employeeMapper.toResponse(user);
    }
}
