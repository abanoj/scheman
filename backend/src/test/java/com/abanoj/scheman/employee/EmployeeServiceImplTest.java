package com.abanoj.scheman.employee;

import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.employee.dto.EmployeeCreateRequestDto;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.mapper.EmployeeMapper;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import com.abanoj.scheman.employee.service.EmployeeServiceImpl;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    private User user;
    private Store store;
    private UUID employeeId;
    private Employee employee;
    private EmployeeResponseDto employeeResponseDto;

    @BeforeEach
    void setUp(){
        employeeId = UUID.randomUUID();

        store = Store.builder()
                .id(UUID.randomUUID())
                .name("Store Test")
                .build();

        user = User.builder()
                .id(employeeId)
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .enabled(true)
                .mustChangePassword(true)
                .build();

        employee = Employee.builder()
                .id(employeeId)
                .dni("Y9107721J")
                .user(user)
                .weeklyContractedHours(40)
                .build();

        employeeResponseDto = new EmployeeResponseDto(
                employeeId,
                employee.getDni(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getId(),
                employee.getPreferredShift(),
                null,
                employee.getWeeklyContractedHours()
        );
    }

    @Nested
    @DisplayName("findAllEmployees")
    class FindAllEmployees {
        @Test
        void shouldReturnPageOfEmployees_whenExists(){
            //given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = new PageImpl<>(List.of(employee));
            given(employeeRepository.findAll(pageable)).willReturn(employeePage);
            given(employeeMapper.toResponseUserEmployeeDto(employee, user)).willReturn(employeeResponseDto);
            //when
            Page<EmployeeResponseDto> result = employeeService.findAllEmployees(pageable);
            //then
            assertThat(result.getContent()).containsExactly(employeeResponseDto);
        }

        @Test
        void shouldReturnEmptyPage_whenDoesNotExistAnyEmployee(){
            //given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> employeePage = Page.empty(pageable);
            given(employeeRepository.findAll(pageable)).willReturn(employeePage);
            //when
            Page<EmployeeResponseDto> result = employeeService.findAllEmployees(pageable);
            //then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {
        private EmployeeCreateRequestDto employeeCreateRequestDto;
        private EmployeeCreateRequestDto employeeCreateWithStoresRequestDto;

        @BeforeEach
        void setUp(){
            employeeCreateRequestDto = new EmployeeCreateRequestDto(
                    employee.getDni(),
                    employee.getUser().getFirstName(),
                    employee.getUser().getLastName(),
                    employee.getUser().getEmail(),
                    employee.getPreferredShift(),
                    employee.getPreferredStores().stream().map(Store::getId).toList(),
                    employee.getWeeklyContractedHours()
            );
            employeeCreateWithStoresRequestDto = new EmployeeCreateRequestDto(
                    employee.getDni(),
                    employee.getUser().getFirstName(),
                    employee.getUser().getLastName(),
                    employee.getUser().getEmail(),
                    employee.getPreferredShift(),
                    Stream.of(store).map(Store::getId).toList(),
                    employee.getWeeklyContractedHours()
            );

        }

        @Test
        void shouldCreateEmployee_whenEmailIsNotInUse_andThereIsNotPreferredStores(){
            //given
            given(userRepository.existsByEmail(employeeCreateRequestDto.email())).willReturn(false);
            given(passwordEncoder.encode(employeeCreateRequestDto.dni())).willReturn("encodedPassword");
            given(employeeMapper.toResponseUserEmployeeDto(any(Employee.class), any(User.class))).willReturn(employeeResponseDto);
            //when
            EmployeeResponseDto result = employeeService.create(employeeCreateRequestDto);
            //then
            assertThat(result).isEqualTo(employeeResponseDto);
            verify(userRepository).save(any(User.class));
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

        @Test
        void shouldCreateEmployee_whenEmailIsNotInUse_andThereIsPreferredStores(){
            //given
            given(userRepository.existsByEmail(employeeCreateWithStoresRequestDto.email())).willReturn(false);
            given(passwordEncoder.encode(employeeCreateWithStoresRequestDto.dni())).willReturn("encodedPassword");
            given(storeRepository.findAllById(employeeCreateWithStoresRequestDto.preferredStoresIDs())).willReturn(List.of(store));
            given(employeeMapper.toResponseUserEmployeeDto(any(Employee.class), any(User.class))).willReturn(employeeResponseDto);
            //when
            EmployeeResponseDto result = employeeService.create(employeeCreateWithStoresRequestDto);
            //then
            assertThat(result).isEqualTo(employeeResponseDto);
            verify(userRepository).save(any(User.class));
            verify(employeeRepository, times(2)).save(any(Employee.class));
        }


        @Test
        void shouldThrowConflict_whenEmailIsAlreadyInUse(){
            //given
            given(userRepository.existsByEmail(employeeCreateRequestDto.email())).willReturn(true);
            //when -> then
            assertThatThrownBy(() -> employeeService.create(employeeCreateRequestDto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Email already in use");
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee{

    }

    @Nested
    @DisplayName("FindEmployeeById")
    class FindEmployeeById{

    }

    @Nested
    @DisplayName("disableEmployeeById")
    class DisableEmployeeById{

    }

    @Nested
    @DisplayName("updateEmployee")
    class EnableEmployeeById{

    }

}
