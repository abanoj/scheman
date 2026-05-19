package com.abanoj.scheman.shift;

import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.dto.UnassignedShiftResponseDto;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shift.mapper.ShiftMapper;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.shift.service.ShiftServiceImpl;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import com.abanoj.scheman.shiftassignment.repository.ShiftAssignmentRepository;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceImplTest {

    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    private ShiftMapper shiftMapper;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    @Captor
    private ArgumentCaptor<List<Shift>> shiftListCaptor;

    private UUID storeId;
    private UUID shiftId;
    private UUID groupId;
    private Store store;
    private Shift shift;
    private ShiftResponseDto shiftResponseDto;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        shiftId = UUID.randomUUID();
        groupId = UUID.randomUUID();

        store = Store.builder()
                .id(storeId)
                .name("Central Store")
                .build();

        shift = Shift.builder()
                .id(shiftId)
                .name("Central Store - Morning")
                .store(store)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .shiftType(ShiftType.MORNING)
                .availableDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY))
                .groupId(groupId)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();

        shiftResponseDto = new ShiftResponseDto(
                shift.getId(),
                shift.getName(),
                shift.getStore().getId(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getShiftType(),
                shift.getAvailableDays(),
                shift.isCrossesMidnight()
        );
    }

    @Nested
    @DisplayName("findAllShiftsByStoreId")
    class FindAllShiftsByStoreId {

        @Test
        void shouldReturnPageOfShifts_whenStoreHasShifts() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Shift> shiftPage = new PageImpl<>(List.of(shift));
            given(shiftRepository.findAllByStoreIdAndDeletedFalse(pageable, storeId)).willReturn(shiftPage);
            given(shiftMapper.toResponseDto(shift)).willReturn(shiftResponseDto);

            // when
            Page<ShiftResponseDto> result = shiftService.findAllShiftsByStoreId(pageable, storeId);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(shiftResponseDto);
        }

        @Test
        void shouldReturnEmptyPage_whenStoreHasNoShifts() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Shift> emptyPage = Page.empty(pageable);
            given(shiftRepository.findAllByStoreIdAndDeletedFalse(pageable, storeId)).willReturn(emptyPage);

            // when
            Page<ShiftResponseDto> result = shiftService.findAllShiftsByStoreId(pageable, storeId);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findShiftById")
    class FindShiftById {

        @Test
        void shouldReturnShift_whenShiftExists() {
            // given
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(shiftId, storeId)).willReturn(Optional.of(shift));
            given(shiftMapper.toResponseDto(shift)).willReturn(shiftResponseDto);

            // when
            ShiftResponseDto result = shiftService.findShiftById(shiftId, storeId);

            // then
            assertThat(result).isEqualTo(shiftResponseDto);
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftDoesNotExist() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(unknownId, storeId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> shiftService.findShiftById(unknownId, storeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    @Nested
    @DisplayName("createShift")
    class CreateShift {

        private ShiftCreateRequestDto shiftCreateRequestDto;

        @BeforeEach
        void setUp() {
            shiftCreateRequestDto = new ShiftCreateRequestDto(
                    "Morning",
                    LocalTime.of(8, 0),
                    LocalTime.of(16, 0),
                    LocalDate.of(2026, 1, 1),
                    ShiftType.MORNING,
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
            );
        }

        @Test
        void shouldCreateShift_whenStoreExists() {
            // given
            Shift mappedShift = Shift.builder()
                    .name("Morning")
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .shiftType(ShiftType.MORNING)
                    .availableDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                    .effectiveFrom(LocalDate.of(2026, 1, 1))
                    .build();

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(shiftMapper.toShift(shiftCreateRequestDto)).willReturn(mappedShift);
            given(shiftMapper.toResponseDto(mappedShift)).willReturn(shiftResponseDto);

            // when
            ShiftResponseDto result = shiftService.createShift(storeId, shiftCreateRequestDto);

            // then
            assertThat(result).isEqualTo(shiftResponseDto);
            assertThat(mappedShift.getStore()).isEqualTo(store);
            assertThat(mappedShift.getGroupId()).isNotNull();
            verify(shiftRepository).save(mappedShift);
        }

        @Test
        void shouldThrowResourceNotFound_whenStoreDoesNotExist() {
            // given
            UUID unknownStoreId = UUID.randomUUID();
            given(storeRepository.findById(unknownStoreId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> shiftService.createShift(unknownStoreId, shiftCreateRequestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(unknownStoreId.toString());
            verify(shiftRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateShift")
    class UpdateShift {

        private ShiftUpdateRequestDto updateDto;

        @BeforeEach
        void setUp() {
            updateDto = new ShiftUpdateRequestDto(
                    "Morning Updated", LocalTime.of(7, 0), LocalTime.of(15, 0),
                    ShiftType.MORNING, Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
            );
        }

        @Test
        void shouldArchiveOldShiftAndCreateNewVersion() {
            // given
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(shiftId, storeId)).willReturn(Optional.of(shift));
            given(shiftMapper.toResponseDto(any(Shift.class))).willReturn(shiftResponseDto);

            // when
            shiftService.updateShift(storeId, shiftId, updateDto);

            // then — old shift archived
            assertThat(shift.isDeleted()).isTrue();
            assertThat(shift.getEffectiveTo()).isEqualTo(LocalDate.now().minusDays(1));

            // then — both saved
            verify(shiftRepository).saveAll(shiftListCaptor.capture());
            List<Shift> saved = shiftListCaptor.getValue();
            assertThat(saved).hasSize(2);
        }

        @Test
        void shouldPreserveGroupIdOnNewVersion() {
            // given
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(shiftId, storeId)).willReturn(Optional.of(shift));
            given(shiftMapper.toResponseDto(any(Shift.class))).willReturn(shiftResponseDto);

            // when
            shiftService.updateShift(storeId, shiftId, updateDto);

            // then
            verify(shiftRepository).saveAll(shiftListCaptor.capture());
            Shift newVersion = shiftListCaptor.getValue().stream()
                    .filter(s -> s != shift)
                    .findFirst()
                    .orElseThrow();

            assertThat(newVersion.getGroupId()).isEqualTo(groupId);
            assertThat(newVersion.getName()).isEqualTo("Morning Updated");
            assertThat(newVersion.getStartTime()).isEqualTo(LocalTime.of(7, 0));
            assertThat(newVersion.getEndTime()).isEqualTo(LocalTime.of(15, 0));
            assertThat(newVersion.getEffectiveFrom()).isEqualTo(LocalDate.now());
            assertThat(newVersion.getStore()).isEqualTo(store);
            assertThat(newVersion.getShiftType()).isEqualTo(ShiftType.MORNING);
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftDoesNotExist() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(unknownId, storeId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> shiftService.updateShift(storeId, unknownId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(shiftRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("deleteShift")
    class DeleteShift {

        @Test
        void shouldSoftDeleteShift_whenShiftExists() {
            // given
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(shiftId, storeId)).willReturn(Optional.of(shift));

            // when
            shiftService.deleteShift(storeId, shiftId);

            // then
            assertThat(shift.isDeleted()).isTrue();
            assertThat(shift.getEffectiveTo()).isEqualTo(LocalDate.now().minusDays(1));
            verify(shiftRepository).save(shift);
        }

        @Test
        void shouldThrowResourceNotFound_whenShiftDoesNotExist() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(shiftRepository.findByIdAndStoreIdAndDeletedFalse(unknownId, storeId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> shiftService.deleteShift(storeId, unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(shiftRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findUnassignedShifts")
    class FindUnassignedShifts {

        // Wednesday 2026-05-13 → week is Monday 2026-05-11 to Sunday 2026-05-17
        private final LocalDate wednesday = LocalDate.of(2026, 5, 13);
        private final LocalDate monday = LocalDate.of(2026, 5, 11);
        private final LocalDate sunday = LocalDate.of(2026, 5, 17);

        @Test
        void shouldReturnEmptyList_whenNoActiveShifts() {
            // given
            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of());

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(shiftAssignmentRepository);
        }

        @Test
        void shouldReturnAllAvailableDays_whenNoAssignmentsExist() {
            // given
            Shift weekdayShift = buildShift("Weekday Morning",
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                    LocalDate.of(2026, 1, 1), null);

            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of(weekdayShift));
            given(shiftAssignmentRepository.findByShiftIdInAndDateBetween(any(), eq(monday), eq(sunday)))
                    .willReturn(List.of());

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).unassignedDays())
                    .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        }

        @Test
        void shouldExcludeFullyAssignedShifts() {
            // given — shift available Mon/Tue, both already assigned
            UUID sid = UUID.randomUUID();
            Shift monTueShift = buildShift(sid, "Mon-Tue",
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                    LocalDate.of(2026, 1, 1), null);

            ShiftAssignment monAssignment = buildAssignment(monTueShift, monday);              // Monday
            ShiftAssignment tueAssignment = buildAssignment(monTueShift, monday.plusDays(1));   // Tuesday

            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of(monTueShift));
            given(shiftAssignmentRepository.findByShiftIdInAndDateBetween(any(), eq(monday), eq(sunday)))
                    .willReturn(List.of(monAssignment, tueAssignment));

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyUnassignedDays_whenPartiallyAssigned() {
            // given — shift available Mon/Tue/Wed, only Monday is assigned
            UUID sid = UUID.randomUUID();
            Shift weekdayShift = buildShift(sid, "Partial",
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                    LocalDate.of(2026, 1, 1), null);

            ShiftAssignment monAssignment = buildAssignment(weekdayShift, monday);

            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of(weekdayShift));
            given(shiftAssignmentRepository.findByShiftIdInAndDateBetween(any(), eq(monday), eq(sunday)))
                    .willReturn(List.of(monAssignment));

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).unassignedDays())
                    .containsExactlyInAnyOrder(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        }

        @Test
        void shouldRespectEffectiveFromBoundary() {
            // given — shift effective from Wednesday, available Mon-Fri
            Shift lateStartShift = buildShift("Late Start",
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                    wednesday, null);  // effective from Wednesday 2026-05-13

            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of(lateStartShift));
            given(shiftAssignmentRepository.findByShiftIdInAndDateBetween(any(), eq(monday), eq(sunday)))
                    .willReturn(List.of());

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then — Mon and Tue excluded because they're before effectiveFrom
            assertThat(result).hasSize(1);
            assertThat(result.get(0).unassignedDays())
                    .containsExactlyInAnyOrder(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        }

        @Test
        void shouldRespectEffectiveToBoundary() {
            // given — shift effective until Tuesday, available Mon-Fri
            Shift endingSoonShift = buildShift("Ending Soon",
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                    LocalDate.of(2026, 1, 1), monday.plusDays(1)); // effectiveTo = Tuesday

            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of(endingSoonShift));
            given(shiftAssignmentRepository.findByShiftIdInAndDateBetween(any(), eq(monday), eq(sunday)))
                    .willReturn(List.of());

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then — only Mon and Tue before effectiveTo
            assertThat(result).hasSize(1);
            assertThat(result.get(0).unassignedDays())
                    .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        }

        @Test
        void shouldHandleMultipleShiftsIndependently() {
            // given
            UUID sid1 = UUID.randomUUID();
            UUID sid2 = UUID.randomUUID();

            Shift shift1 = buildShift(sid1, "Shift A",
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                    LocalDate.of(2026, 1, 1), null);
            Shift shift2 = buildShift(sid2, "Shift B",
                    Set.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY),
                    LocalDate.of(2026, 1, 1), null);

            // shift1 Monday assigned, shift2 nothing assigned
            ShiftAssignment monAssignment = buildAssignment(shift1, monday);

            given(shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday))
                    .willReturn(List.of(shift1, shift2));
            given(shiftAssignmentRepository.findByShiftIdInAndDateBetween(any(), eq(monday), eq(sunday)))
                    .willReturn(List.of(monAssignment));

            // when
            List<UnassignedShiftResponseDto> result = shiftService.findUnassignedShifts(storeId, wednesday);

            // then
            assertThat(result).hasSize(2);

            UnassignedShiftResponseDto resultA = result.stream()
                    .filter(r -> r.shiftName().equals("Shift A")).findFirst().orElseThrow();
            UnassignedShiftResponseDto resultB = result.stream()
                    .filter(r -> r.shiftName().equals("Shift B")).findFirst().orElseThrow();

            assertThat(resultA.unassignedDays()).containsExactly(DayOfWeek.TUESDAY);
            assertThat(resultB.unassignedDays())
                    .containsExactlyInAnyOrder(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY);
        }

        // ── Helpers ──

        private Shift buildShift(String name, Set<DayOfWeek> days, LocalDate from, LocalDate to) {
            return buildShift(UUID.randomUUID(), name, days, from, to);
        }

        private Shift buildShift(UUID id, String name, Set<DayOfWeek> days, LocalDate from, LocalDate to) {
            return Shift.builder()
                    .id(id)
                    .name(name)
                    .store(store)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .shiftType(ShiftType.MORNING)
                    .availableDays(new HashSet<>(days))
                    .groupId(UUID.randomUUID())
                    .effectiveFrom(from)
                    .effectiveTo(to)
                    .build();
        }

        private ShiftAssignment buildAssignment(Shift shift, LocalDate date) {
            return ShiftAssignment.builder()
                    .id(UUID.randomUUID())
                    .shift(shift)
                    .date(date)
                    .build();
        }
    }
}
