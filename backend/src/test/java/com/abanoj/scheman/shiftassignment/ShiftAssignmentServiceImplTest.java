package com.abanoj.scheman.shiftassignment;

import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import com.abanoj.scheman.shiftassignment.mapper.ShiftAssignmentMapper;
import com.abanoj.scheman.shiftassignment.repository.ShiftAssignmentRepository;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentServiceImpl;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ShiftAssignmentServiceImplTest {
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    private ShiftAssignmentMapper shiftAssignmentMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftAssignmentServiceImpl shiftAssignmentService;

    private UUID id;
    private UUID shiftId;
    private UUID employeeId;
    private Shift shift;
    private Employee employee;
    private ShiftAssignment shiftAssignment;
    private ShiftAssignmentResponseDto shiftAssignmentResponseDto;
    private Pageable pageable;

    @BeforeEach
    void setUp(){
        pageable = PageRequest.of(0, 10);
        id = UUID.randomUUID();
        shiftId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        shift = Shift.builder()
                .id(shiftId)
                .startTime(LocalTime.of(8,0))
                .endTime(LocalTime.of(16,0))
                .name("Test Shift")
                .build();

        employee = Employee.builder()
                .id(employeeId)
                .build();

        shiftAssignment = ShiftAssignment.builder()
                .id(id)
                .date(LocalDate.of(2026,6,1))
                .employee(employee)
                .shift(shift)
                .build();

        shiftAssignmentResponseDto = new ShiftAssignmentResponseDto(
                shiftAssignment.getId(),
                shiftAssignment.getDate(),
                shiftAssignment.getEmployee().getId(),
                shiftAssignment.getShift().getId()
        );
    }

    @Nested
    @DisplayName("findAllShiftsAssignmentsByShiftId")
    class FindAllShiftsAssignmentsByShiftId {
        @Test
        void shouldReturnPageOfShiftAssignment_whenShiftHasShiftAssignment(){
            //given
            Page<ShiftAssignment> shiftAssignmentPage = new PageImpl<>(List.of(shiftAssignment));
            given(shiftAssignmentRepository.findAllByShiftId(pageable, shiftId)).willReturn(shiftAssignmentPage);
            given(shiftAssignmentMapper.toResponseDto(shiftAssignment)).willReturn(shiftAssignmentResponseDto);
            //when
            Page<ShiftAssignmentResponseDto> result = shiftAssignmentService.findAllShiftsAssignmentsByShiftId(pageable, shiftId);
            //then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(shiftAssignmentResponseDto);
        }

        @Test
        void shouldReturnEmptyPage_whenShiftHasNoShiftAssignment(){
            //given
            Page<ShiftAssignment> shiftAssignmentPage = Page.empty(pageable);
            given(shiftAssignmentRepository.findAllByShiftId(pageable, shiftId)).willReturn(shiftAssignmentPage);
            //when
            Page<ShiftAssignmentResponseDto> result = shiftAssignmentService.findAllShiftsAssignmentsByShiftId(pageable, shiftId);
            //then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllShiftsAssignmentsByEmployeeId")
    class FindAllShiftsAssignmentsByEmployeeId {
        @Test
        void shouldReturnPageOfShiftsAssignment_whenEmployeeHasShiftAssignment(){
            //given
            Page<ShiftAssignment> shiftAssignmentPage = new PageImpl<>(List.of(shiftAssignment));
            given(shiftAssignmentRepository.findAllByEmployeeId(pageable, employeeId)).willReturn(shiftAssignmentPage);
            given(shiftAssignmentMapper.toResponseDto(shiftAssignment)).willReturn(shiftAssignmentResponseDto);
            //when
            Page<ShiftAssignmentResponseDto> result = shiftAssignmentService.findAllShiftsAssignmentsByEmployeeId(pageable, employeeId);
            //then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(shiftAssignmentResponseDto);
        }

        @Test
        void shouldReturnEmptyPage_whenEmployeeHasNoShiftAssignment(){
            //given
            Page<ShiftAssignment> shiftAssignmentPage = Page.empty(pageable);
            given(shiftAssignmentRepository.findAllByEmployeeId(pageable, employeeId)).willReturn(shiftAssignmentPage);
            //when
            Page<ShiftAssignmentResponseDto> result = shiftAssignmentService.findAllShiftsAssignmentsByEmployeeId(pageable, employeeId);
            //then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findShiftAssignmentById")
    class FindShiftAssignmentById {
        @Test
        void shouldReturnShiftAssignment_whenShiftAssignmentExists(){
            //given
            given(shiftAssignmentRepository.findByIdAndShiftId(id, shiftId)).willReturn(Optional.of(shiftAssignment));
            given(shiftAssignmentMapper.toResponseDto(shiftAssignment)).willReturn(shiftAssignmentResponseDto);
            //when
            ShiftAssignmentResponseDto result = shiftAssignmentService.findShiftAssignmentById(shiftId, id);
            //then
            assertThat(result).isEqualTo(shiftAssignmentResponseDto);
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftAssignmentDoesNotExists(){
            //given
            UUID uuid = UUID.randomUUID();
            given(shiftAssignmentRepository.findByIdAndShiftId(uuid, shiftId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.findShiftAssignmentById(shiftId, uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(uuid.toString());
            verify(shiftAssignmentMapper, never()).toResponseDto(any());
        }
    }

    @Nested
    @DisplayName("createShiftAssignment")
    class CreateShiftAssignment {
        private ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto;

        @BeforeEach
        void setUp(){
            shiftAssignmentCreateRequestDto = new ShiftAssignmentCreateRequestDto(
                    shiftAssignment.getDate(),
                    shiftAssignment.getEmployee().getId()
            );
        }

        @Test
        void shouldCreateShiftAssignment_whenShiftAndEmployeeExist(){
            //given
            given(shiftRepository.findById(shiftId)).willReturn(Optional.of(shift));
            given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
            given(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(
                    employeeId,
                    shiftAssignmentCreateRequestDto.date().minusDays(1),
                    shiftAssignmentCreateRequestDto.date()
            )).willReturn(List.of());
            given(shiftAssignmentMapper.toShiftAssignment(shiftAssignmentCreateRequestDto)).willReturn(shiftAssignment);
            given(shiftAssignmentMapper.toResponseDto(shiftAssignment)).willReturn(shiftAssignmentResponseDto);
            //when
            ShiftAssignmentResponseDto result = shiftAssignmentService.createShiftAssignment(shiftId, shiftAssignmentCreateRequestDto);
            //then
            assertThat(result).isEqualTo(shiftAssignmentResponseDto);
            verify(shiftAssignmentRepository).save(shiftAssignment);
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftDoesNotExist(){
            //given
            UUID unknownShiftId = UUID.randomUUID();
            given(shiftRepository.findById(unknownShiftId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.createShiftAssignment(unknownShiftId, shiftAssignmentCreateRequestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownShiftId.toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFound_whenEmployeeDoesNotExist(){
            //given
            UUID unknownEmployeeId = UUID.randomUUID();
            ShiftAssignmentCreateRequestDto requestWithUnknownEmployee = new ShiftAssignmentCreateRequestDto(
                    LocalDate.of(2026, 6, 1),
                    unknownEmployeeId
            );
            given(shiftRepository.findById(shiftId)).willReturn(Optional.of(shift));
            given(employeeRepository.findById(unknownEmployeeId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.createShiftAssignment(shiftId, requestWithUnknownEmployee))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownEmployeeId.toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflict_whenEmployeeHasOverlappingShift(){
            //given
            UUID newShiftId = UUID.randomUUID();
            Shift newShift = Shift.builder()
                    .id(newShiftId)
                    .name("New Shift")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();
            given(shiftRepository.findById(newShiftId)).willReturn(Optional.of(newShift));
            given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
            given(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(
                    employeeId,
                    shiftAssignmentCreateRequestDto.date().minusDays(1),
                    shiftAssignmentCreateRequestDto.date()
            )).willReturn(List.of(shiftAssignment));
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.createShiftAssignment(newShiftId, shiftAssignmentCreateRequestDto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(shiftAssignmentCreateRequestDto.date().toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateShiftAssignment")
    class UpdateShiftAssignment {

        private ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto;
        private ShiftAssignmentResponseDto shiftAssignmentUpdateResponseDto;

        @BeforeEach
        void setUp(){
            shiftAssignmentUpdateRequestDto = new ShiftAssignmentUpdateRequestDto(
                    shiftAssignment.getDate().plusDays(1),
                    shiftAssignment.getEmployee().getId()
            );
            shiftAssignmentUpdateResponseDto = new ShiftAssignmentResponseDto(
                    id,
                    shiftAssignment.getDate().plusDays(1),
                    shiftAssignment.getEmployee().getId(),
                    shiftAssignment.getShift().getId()
            );
        }

        @Test
        void shouldUpdateShiftAssignment_whenShiftAndEmployeesAndShiftAssignmentExist(){
            //given
            given(shiftRepository.findById(shiftId)).willReturn(Optional.of(shift));
            given(shiftAssignmentRepository.findByIdAndShiftId(id, shiftId)).willReturn(Optional.of(shiftAssignment));
            given(employeeRepository.findById(shiftAssignmentUpdateRequestDto.employeeId())).willReturn(Optional.of(employee));
            given(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(
                    employeeId,
                    shiftAssignmentUpdateRequestDto.date().minusDays(1),
                    shiftAssignmentUpdateRequestDto.date()
            )).willReturn(List.of());
            given(shiftAssignmentMapper.toResponseDto(shiftAssignment)).willReturn(shiftAssignmentUpdateResponseDto);
            //when
            ShiftAssignmentResponseDto result = shiftAssignmentService.updateShiftAssignment(shiftId, id, shiftAssignmentUpdateRequestDto);
            //then
            assertThat(result).isEqualTo(shiftAssignmentUpdateResponseDto);
            verify(shiftAssignmentMapper).updateShiftAssignmentFromDto(shiftAssignmentUpdateRequestDto, shiftAssignment);
            verify(shiftAssignmentRepository).save(shiftAssignment);
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            given(shiftRepository.findById(unknownId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(()-> shiftAssignmentService.updateShiftAssignment(unknownId, id, shiftAssignmentUpdateRequestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftAssignmentDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            given(shiftRepository.findById(shiftId)).willReturn(Optional.of(shift));
            given(shiftAssignmentRepository.findByIdAndShiftId(unknownId, shiftId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(()-> shiftAssignmentService.updateShiftAssignment(shiftId, unknownId, shiftAssignmentUpdateRequestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFound_whenEmployeeDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            ShiftAssignmentUpdateRequestDto shiftAssignmentWithInvalidEmployee =
                    new ShiftAssignmentUpdateRequestDto(shiftAssignmentUpdateRequestDto.date(), unknownId);
            given(shiftRepository.findById(shiftId)).willReturn(Optional.of(shift));
            given(shiftAssignmentRepository.findByIdAndShiftId(id, shiftId)).willReturn(Optional.of(shiftAssignment));
            given(employeeRepository.findById(unknownId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(()-> shiftAssignmentService.updateShiftAssignment(shiftId, id, shiftAssignmentWithInvalidEmployee))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflict_whenEmployeeHasOverlappingShift(){
            //given
            Shift existingShift = Shift.builder()
                    .id(UUID.randomUUID())
                    .name("Existing Shift")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();
            ShiftAssignment existingAssignment = ShiftAssignment.builder()
                    .id(UUID.randomUUID())
                    .date(shiftAssignmentUpdateRequestDto.date())
                    .employee(employee)
                    .shift(existingShift)
                    .build();
            given(shiftRepository.findById(shiftId)).willReturn(Optional.of(shift));
            given(shiftAssignmentRepository.findByIdAndShiftId(id, shiftId)).willReturn(Optional.of(shiftAssignment));
            given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
            given(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(
                    employeeId,
                    shiftAssignmentUpdateRequestDto.date().minusDays(1),
                    shiftAssignmentUpdateRequestDto.date()
            )).willReturn(List.of(existingAssignment));
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.updateShiftAssignment(shiftId, id, shiftAssignmentUpdateRequestDto))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(shiftAssignmentUpdateRequestDto.date().toString());
            verify(shiftAssignmentRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("findWeeklyAssignmentsByEmployee")
    class FindWeeklyAssignmentsByEmployee {

        private LocalDate date;
        private LocalDate monday;
        private LocalDate sunday;

        @BeforeEach
        void setUp(){
            date = LocalDate.now();
            monday = date.with(DayOfWeek.MONDAY);
            sunday = date.with(DayOfWeek.SUNDAY);
        }

        @Test
        void shouldReturnListOfShiftAssignment_whenEmployeeExist(){
            //given
            given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
            given(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(employeeId, monday, sunday)).willReturn(List.of(shiftAssignment));
            given(shiftAssignmentMapper.toResponseDto(shiftAssignment)).willReturn(shiftAssignmentResponseDto);
            //when
            List<ShiftAssignmentResponseDto> result = shiftAssignmentService.findWeeklyAssignmentsByEmployee(employeeId, date);
            //then
            assertThat(result).containsExactly(shiftAssignmentResponseDto);
        }

        @Test
        void shouldReturnEmptyList_whenEmployeeHasNoAssignments(){
            //given
            given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
            given(shiftAssignmentRepository.findByEmployeeIdAndDateBetween(employeeId, monday, sunday)).willReturn(List.of());
            //when
            List<ShiftAssignmentResponseDto> result = shiftAssignmentService.findWeeklyAssignmentsByEmployee(employeeId, date);
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldThrowResourceNotFound_whenEmployeeDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            given(employeeRepository.findById(unknownId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.findWeeklyAssignmentsByEmployee(unknownId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    @Nested
    @DisplayName("deleteShiftAssignment")
    class DeleteShiftAssignment {
        @Test
        void shouldDeleteShiftAssignment_whenExist(){
            //given
            given(shiftAssignmentRepository.existsByIdAndShiftId(id, shiftId)).willReturn(true);
            //when
            shiftAssignmentService.delete(shiftId, id);
            //then
            verify(shiftAssignmentRepository).deleteById(id);

        }
        @Test
        void shouldThrowResourceNotFound_whenShiftAssignmentDoesNotExist(){
            //given
            given(shiftAssignmentRepository.existsByIdAndShiftId(id, shiftId)).willReturn(false);
            //when -> then
            assertThatThrownBy(() -> shiftAssignmentService.delete(shiftId, id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());
            verify(shiftAssignmentRepository, never()).deleteById(any());
        }
    }
}
