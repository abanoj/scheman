package com.abanoj.scheman.employee.service;

import com.abanoj.scheman.employee.dto.EmployeeCreateRequestDto;
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
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    public static final String NOT_FOUND_EMPLOYEE_MESSAGE = "Not found employee with id: ";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> findAllEmployees(Pageable pageable) {
        return employeeRepository
                .findAll(pageable)
                .map(employee -> employeeMapper.toResponseUserEmployeeDto(employee, employee.getUser()));
    }

    @Override
    @Transactional
    public EmployeeResponseDto create(EmployeeCreateRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.dni()))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .mustChangePassword(true)
                .build();

        userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .dni(request.dni())
                .preferredShift(request.preferredShift())
                .weeklyContractedHours(request.weeklyContractedHours())
                .build();

        employeeRepository.save(employee);

        if (request.preferredStoresIDs() != null && !request.preferredStoresIDs().isEmpty()) {
            List<Store> stores = storeRepository.findAllById(request.preferredStoresIDs());
            stores.forEach(employee::addStore);
            employeeRepository.save(employee);
        }

        log.info("New employee created: {} {}", request.firstName(), request.lastName());
        return employeeMapper.toResponseUserEmployeeDto(employee, user);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(UUID employeeId, EmployeeUpdateRequestDto employeeUpdateRequestDto) {
        Employee employee = employeeRepository
                .findByIdWithUser(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException(NOT_FOUND_EMPLOYEE_MESSAGE + employeeId));
        User user = employee.getUser();
        employeeMapper.updateUserFromDto(employeeUpdateRequestDto, user);
        employeeMapper.updateEmployeeFromDto(employeeUpdateRequestDto, employee);

        if (employeeUpdateRequestDto.preferredStoresIds() != null) {
            List<Store> currentStores = new ArrayList<>(employee.getPreferredStores());
            currentStores.forEach(employee::removeStore);

            List<Store> newStores = storeRepository.findAllById(employeeUpdateRequestDto.preferredStoresIds());
            newStores.forEach(employee::addStore);
        }

        userRepository.save(user);
        employeeRepository.save(employee);
        log.debug("Updated employee with id {}", employeeId);
        return employeeMapper.toResponseUserEmployeeDto(employee, user);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto findEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository
                .findByIdWithUser(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException(NOT_FOUND_EMPLOYEE_MESSAGE + employeeId));
        User user = employee.getUser();
        return employeeMapper.toResponseUserEmployeeDto(employee, user);
    }

    @Override
    @Transactional
    public void disableEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository
                .findByIdWithUser(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException(NOT_FOUND_EMPLOYEE_MESSAGE + employeeId));
        User user = employee.getUser();
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Employee disabled {}", user.getEmail());
    }

    @Override
    @Transactional
    public void enableEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository
                .findByIdWithUser(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException(NOT_FOUND_EMPLOYEE_MESSAGE + employeeId));
        User user = employee.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Employee enabled {}", user.getEmail());
    }
}
