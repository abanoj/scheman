package com.abanoj.scheman.shiftassignment;

import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.config.JpaAuditingConfig;
import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import com.abanoj.scheman.shiftassignment.repository.ShiftAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ShiftAssignmentRepositoryTest {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Autowired
    private TestEntityManager entityManager;

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0,10);
    private Shift shift;
    private Employee employee;

    @BeforeEach
    void setUp(){
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@mail.com")
                .password("password")
                .role(Role.EMPLOYEE)
                .build();
        entityManager.persist(user);
        employee = Employee.builder()
                .dni("Y8190012J")
                .user(user)
                .build();
        entityManager.persist(employee);
        shift = Shift.builder()
                .name("Turno Mañana")
                .startTime(LocalTime.of(7,30))
                .endTime(LocalTime.of(15,30))
                .shiftType(ShiftType.MORNING)
                .effectiveFrom(LocalDate.of(2026,1,1))
                .build();
        entityManager.persistAndFlush(shift);
    }

    private void persistShiftAssignment(LocalDate date, Employee employee, Shift shift){
        ShiftAssignment shiftAssignment = ShiftAssignment.builder()
                .date(date)
                .employee(employee)
                .shift(shift)
                .build();
        entityManager.persistAndFlush(shiftAssignment);
    }

    @Nested
    @DisplayName("findAllByShiftId")
    class FindAllByShiftId {
        @Test
        void shouldReturnAllShiftsAssignments_whenShiftExists(){
            //given
            persistShiftAssignment(
                    LocalDate.of(2026,5,26),
                    employee,
                    shift
            );
            persistShiftAssignment(
                    LocalDate.of(2026,5,27),
                    employee,
                    shift
            );
            entityManager.clear();
            //when
            Page<ShiftAssignment> result = shiftAssignmentRepository.findAllByShiftId(DEFAULT_PAGEABLE, shift.getId());
            //then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ShiftAssignment::getDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2026,5,26),
                            LocalDate.of(2026,5,27)
                    );
        }
        @Test
        void shouldReturnEmptyPage_whenShiftHasNoShiftAssignment(){
            //given
            entityManager.clear();
            //when
            Page<ShiftAssignment> result = shiftAssignmentRepository.findAllByShiftId(DEFAULT_PAGEABLE, shift.getId());
            //then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
        @Test
        void shouldNotReturnShiftAssignmentFromAnotherShift(){
            //given
            Shift shiftAfternoon = Shift.builder()
                    .name("Turno Tarde")
                    .startTime(LocalTime.of(15,30))
                    .endTime(LocalTime.of(23,30))
                    .shiftType(ShiftType.AFTERNOON)
                    .effectiveFrom(LocalDate.of(2026,1,1))
                    .build();
            entityManager.persistAndFlush(shiftAfternoon);
            persistShiftAssignment(
                    LocalDate.of(2026,5,26),
                    employee,
                    shift
            );
            persistShiftAssignment(
                    LocalDate.of(2026,2,1),
                    employee,
                    shiftAfternoon
            );
            entityManager.clear();
            //when
            Page<ShiftAssignment> result = shiftAssignmentRepository.findAllByShiftId(DEFAULT_PAGEABLE, shiftAfternoon.getId());
            //then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent())
                    .extracting(ShiftAssignment::getDate)
                    .containsExactly(LocalDate.of(2026,2,1));
        }

    }

    @Nested
    @DisplayName("findByEmployeeIdAndDateBetween")
    class FindByEmployeeIdAndDateBetween{
        @Test
        void shouldReturnShiftAssignment_whenTheyAreBetweenRangeDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,1,2),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,3,5),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,7,1),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            employee.getId(),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2026,1,2),
                            LocalDate.of(2026,3,5)
                    )
                    .doesNotContain(LocalDate.of(2026,7,1));
        }

        @Test
        void shouldReturnShiftAssignment_whenTheyAreInBoundRangeDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,1,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,6,30),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            employee.getId(),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
        }

        @Test
        void shouldReturnEmpty_whenThereAreNotShiftAssignmentBetweenRangeDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,1,2),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,3,5),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,6,10),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            employee.getId(),
                            LocalDate.of(2026,7,1),
                            LocalDate.of(2026,12,31)
                    );
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenThereAreNotShiftAssignmentForTheEmployee(){
            //given
            persistShiftAssignment(LocalDate.of(2026,1,2),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,3,5),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,6,10),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            UUID.randomUUID(),
                            LocalDate.of(2026,7,1),
                            LocalDate.of(2026,12,31)
                    );
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnResultsOrderedByDateAsc(){
            //given
            persistShiftAssignment(LocalDate.of(2026,6,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,2,15),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,4,10),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            employee.getId(),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,12,31)
                    );
            //then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactly(
                            LocalDate.of(2026,2,15),
                            LocalDate.of(2026,4,10),
                            LocalDate.of(2026,6,1)
                    );
        }

        @Test
        void shouldNotReturnShiftAssignmentsFromAnotherEmployee(){
            //given
            User anotherUser = User.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@mail.com")
                    .password("password")
                    .role(Role.EMPLOYEE)
                    .build();
            entityManager.persist(anotherUser);
            Employee anotherEmployee = Employee.builder()
                    .dni("X1234567A")
                    .user(anotherUser)
                    .build();
            entityManager.persist(anotherEmployee);
            persistShiftAssignment(LocalDate.of(2026,2,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,2,1),anotherEmployee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            employee.getId(),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(1);
            assertThat(result).extracting(sa -> sa.getEmployee().getId())
                    .containsOnly(employee.getId());
        }

        @Test
        void shouldReturnShiftAssignment_whenStartDateEqualsEndDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,3,15),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByEmployeeIdAndDateBetween(
                            employee.getId(),
                            LocalDate.of(2026,3,15),
                            LocalDate.of(2026,3,15)
                    );
            //then
            assertThat(result).hasSize(1);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactly(LocalDate.of(2026,3,15));
        }
    }

    @Nested
    @DisplayName("findByShiftIdInAndDateBetween")
    class FindByShiftIdInAndDateBetween{

        private Shift shiftAfternoon;

        @BeforeEach
        void setUpShifts(){
            shiftAfternoon = Shift.builder()
                    .name("Turno Tarde")
                    .startTime(LocalTime.of(15,30))
                    .endTime(LocalTime.of(23,30))
                    .shiftType(ShiftType.AFTERNOON)
                    .effectiveFrom(LocalDate.of(2026,1,1))
                    .build();
            entityManager.persistAndFlush(shiftAfternoon);
        }

        @Test
        void shouldReturnShiftAssignments_whenTheyAreBetweenRangeDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,2,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,4,15),employee, shiftAfternoon);
            persistShiftAssignment(LocalDate.of(2026,8,1),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(shift.getId(), shiftAfternoon.getId()),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2026,2,1),
                            LocalDate.of(2026,4,15)
                    )
                    .doesNotContain(LocalDate.of(2026,8,1));
        }

        @Test
        void shouldReturnShiftAssignments_whenTheyAreInBoundRangeDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,1,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,6,30),employee, shiftAfternoon);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(shift.getId(), shiftAfternoon.getId()),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
        }

        @Test
        void shouldReturnEmpty_whenNoShiftAssignmentsBetweenRangeDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,2,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,3,10),employee, shiftAfternoon);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(shift.getId(), shiftAfternoon.getId()),
                            LocalDate.of(2026,7,1),
                            LocalDate.of(2026,12,31)
                    );
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenShiftIdsDoNotMatch(){
            //given
            persistShiftAssignment(LocalDate.of(2026,2,1),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(UUID.randomUUID()),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,12,31)
                    );
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldNotReturnShiftAssignmentsFromShiftsNotInList(){
            //given
            persistShiftAssignment(LocalDate.of(2026,3,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,3,2),employee, shiftAfternoon);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(shift.getId()),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(1);
            assertThat(result).extracting(sa -> sa.getShift().getId())
                    .containsOnly(shift.getId());
        }

        @Test
        void shouldReturnShiftAssignmentsFromMultipleShifts(){
            //given
            Shift shiftNight = Shift.builder()
                    .name("Turno Noche")
                    .startTime(LocalTime.of(23,30))
                    .endTime(LocalTime.of(7,30))
                    .shiftType(ShiftType.NIGHT)
                    .effectiveFrom(LocalDate.of(2026,1,1))
                    .build();
            entityManager.persistAndFlush(shiftNight);
            persistShiftAssignment(LocalDate.of(2026,3,1),employee, shift);
            persistShiftAssignment(LocalDate.of(2026,3,2),employee, shiftAfternoon);
            persistShiftAssignment(LocalDate.of(2026,3,3),employee, shiftNight);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(shift.getId(), shiftAfternoon.getId(), shiftNight.getId()),
                            LocalDate.of(2026,1,1),
                            LocalDate.of(2026,6,30)
                    );
            //then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(sa -> sa.getShift().getId())
                    .containsExactlyInAnyOrder(
                            shift.getId(),
                            shiftAfternoon.getId(),
                            shiftNight.getId()
                    );
        }

        @Test
        void shouldReturnShiftAssignment_whenStartDateEqualsEndDate(){
            //given
            persistShiftAssignment(LocalDate.of(2026,5,20),employee, shift);
            entityManager.clear();
            //when
            List<ShiftAssignment> result = shiftAssignmentRepository
                    .findByShiftIdInAndDateBetween(
                            List.of(shift.getId()),
                            LocalDate.of(2026,5,20),
                            LocalDate.of(2026,5,20)
                    );
            //then
            assertThat(result).hasSize(1);
            assertThat(result).extracting(ShiftAssignment::getDate)
                    .containsExactly(LocalDate.of(2026,5,20));
        }
    }
}
