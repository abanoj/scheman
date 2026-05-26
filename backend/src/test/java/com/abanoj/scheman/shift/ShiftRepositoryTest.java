package com.abanoj.scheman.shift;

import com.abanoj.scheman.config.JpaAuditingConfig;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.store.entity.Store;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ShiftRepositoryTest {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .name("Store Central")
                .address("Calle Principal 1")
                .build();
        entityManager.persistAndFlush(store);
    }

    private Shift persistShift(String name, Store targetStore, boolean deleted,
                               LocalDate effectiveFrom, LocalDate effectiveTo,
                               UUID groupId) {
        Shift shift = Shift.builder()
                .name(name)
                .store(targetStore)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .shiftType(ShiftType.MORNING)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .groupId(groupId)
                .deleted(deleted)
                .build();
        entityManager.persistAndFlush(shift);
        return shift;
    }

    @Nested
    @DisplayName("findByIdAndStoreIdAndDeletedFalse")
    class FindByIdAndStoreIdAndDeletedFalse {

        @Test
        void shouldReturnShift_whenExistsAndNotDeleted() {
            // given
            Shift shift = persistShift("Turno Mañana", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findByIdAndStoreIdAndDeletedFalse(shift.getId(), store.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Turno Mañana");
            assertThat(result.get().getId()).isEqualTo(shift.getId());
        }

        @Test
        void shouldReturnEmpty_whenShiftIsDeleted() {
            // given
            Shift shift = persistShift("Turno Mañana", store, true,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findByIdAndStoreIdAndDeletedFalse(shift.getId(), store.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenStoreIdDoesNotMatch() {
            // given
            Shift shift = persistShift("Turno Mañana", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findByIdAndStoreIdAndDeletedFalse(shift.getId(), UUID.randomUUID());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenShiftIdDoesNotExist() {
            // when
            Optional<Shift> result = shiftRepository
                    .findByIdAndStoreIdAndDeletedFalse(UUID.randomUUID(), store.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByStoreIdAndDeletedFalse")
    class FindAllByStoreIdAndDeletedFalse {

        @Test
        void shouldReturnActiveShifts_whenStoreHasShifts() {
            // given
            persistShift("Turno Mañana", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            persistShift("Turno Tarde", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Page<Shift> result = shiftRepository
                    .findAllByStoreIdAndDeletedFalse(PageRequest.of(0, 10), store.getId());

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Shift::getName)
                    .containsExactlyInAnyOrder("Turno Mañana", "Turno Tarde");
        }

        @Test
        void shouldExcludeDeletedShifts() {
            // given
            persistShift("Turno Activo", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            persistShift("Turno Borrado", store, true,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Page<Shift> result = shiftRepository
                    .findAllByStoreIdAndDeletedFalse(PageRequest.of(0, 10), store.getId());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Turno Activo");
        }

        @Test
        void shouldNotReturnShiftsFromAnotherStore() {
            // given
            Store otherStore = Store.builder().name("Otra Sucursal").build();
            entityManager.persistAndFlush(otherStore);

            persistShift("Turno Store A", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            persistShift("Turno Store B", otherStore, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Page<Shift> result = shiftRepository
                    .findAllByStoreIdAndDeletedFalse(PageRequest.of(0, 10), store.getId());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Turno Store A");
        }

        @Test
        void shouldReturnEmptyPage_whenStoreHasNoShifts() {
            // given
            entityManager.clear();

            // when
            Page<Shift> result = shiftRepository
                    .findAllByStoreIdAndDeletedFalse(PageRequest.of(0, 10), store.getId());

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void shouldRespectPagination() {
            // given
            for (int i = 1; i <= 5; i++) {
                persistShift("Turno " + i, store, false,
                        LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            }
            entityManager.clear();

            // when
            Page<Shift> firstPage = shiftRepository
                    .findAllByStoreIdAndDeletedFalse(PageRequest.of(0, 2), store.getId());
            Page<Shift> secondPage = shiftRepository
                    .findAllByStoreIdAndDeletedFalse(PageRequest.of(1, 2), store.getId());

            // then
            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(secondPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(5);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findActiveShiftByGroupId")
    class FindActiveShiftByGroupId {

        @Test
        void shouldReturnShift_whenDateIsWithinEffectiveRange() {
            // given
            UUID groupId = UUID.randomUUID();
            persistShift("Turno Mañana V1", store, false,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, LocalDate.of(2025, 3, 15));

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Turno Mañana V1");
        }

        @Test
        void shouldReturnShift_whenEffectiveToIsNull() {
            // given
            UUID groupId = UUID.randomUUID();
            persistShift("Turno Permanente", store, false,
                    LocalDate.of(2025, 1, 1), null, groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, LocalDate.of(2030, 12, 31));

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Turno Permanente");
        }

        @Test
        void shouldReturnShift_whenDateEqualsEffectiveFrom() {
            // given
            UUID groupId = UUID.randomUUID();
            LocalDate effectiveFrom = LocalDate.of(2025, 3, 1);
            persistShift("Turno Boundary", store, false,
                    effectiveFrom, LocalDate.of(2025, 12, 31), groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, effectiveFrom);

            // then
            assertThat(result).isPresent();
        }

        @Test
        void shouldReturnShift_whenDateEqualsEffectiveTo() {
            // given
            UUID groupId = UUID.randomUUID();
            LocalDate effectiveTo = LocalDate.of(2025, 6, 30);
            persistShift("Turno Boundary", store, false,
                    LocalDate.of(2025, 1, 1), effectiveTo, groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, effectiveTo);

            // then
            assertThat(result).isPresent();
        }

        @Test
        void shouldReturnEmpty_whenDateIsBeforeEffectiveFrom() {
            // given
            UUID groupId = UUID.randomUUID();
            persistShift("Turno Futuro", store, false,
                    LocalDate.of(2025, 6, 1), null, groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, LocalDate.of(2025, 1, 1));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenDateIsAfterEffectiveTo() {
            // given
            UUID groupId = UUID.randomUUID();
            persistShift("Turno Expirado", store, false,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, LocalDate.of(2025, 12, 1));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenShiftIsDeleted() {
            // given
            UUID groupId = UUID.randomUUID();
            persistShift("Turno Borrado", store, true,
                    LocalDate.of(2025, 1, 1), null, groupId);
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(groupId, LocalDate.of(2025, 3, 15));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenGroupIdDoesNotMatch() {
            // given
            persistShift("Turno Mañana", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            Optional<Shift> result = shiftRepository
                    .findActiveShiftByGroupId(UUID.randomUUID(), LocalDate.of(2025, 3, 15));

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveShiftsByStoreIdAndDateRange")
    class FindActiveShiftsByStoreIdAndDateRange {

        @Test
        void shouldReturnShifts_whenTheyOverlapWithDateRange() {
            // given
            persistShift("Turno Q1", store, false,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31), UUID.randomUUID());
            persistShift("Turno Q2", store, false,
                    LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30), UUID.randomUUID());
            entityManager.clear();

            // when - query range covers both shifts
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Shift::getName)
                    .containsExactlyInAnyOrder("Turno Q1", "Turno Q2");
        }

        @Test
        void shouldReturnOpenEndedShifts_whenTheyOverlapWithDateRange() {
            // given
            persistShift("Turno Permanente", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31));

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Turno Permanente");
        }

        @Test
        void shouldReturnShift_whenItPartiallyOverlapsWithRangeStart() {
            // given - shift ends mid-range
            persistShift("Turno Parcial", store, false,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 15), UUID.randomUUID());
            entityManager.clear();

            // when - query starts before shift ends
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 3, 1), LocalDate.of(2025, 6, 30));

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        void shouldReturnShift_whenItPartiallyOverlapsWithRangeEnd() {
            // given - shift starts mid-range
            persistShift("Turno Parcial", store, false,
                    LocalDate.of(2025, 5, 1), LocalDate.of(2025, 12, 31), UUID.randomUUID());
            entityManager.clear();

            // when - query ends after shift starts
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        void shouldExcludeShifts_whenTheyEndBeforeDateRange() {
            // given
            persistShift("Turno Pasado", store, false,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 28), UUID.randomUUID());
            entityManager.clear();

            // when - query range is after shift ends
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 12, 31));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldExcludeShifts_whenTheyStartAfterDateRange() {
            // given
            persistShift("Turno Futuro", store, false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 31), UUID.randomUUID());
            entityManager.clear();

            // when - query range is before shift starts
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldExcludeDeletedShifts() {
            // given
            persistShift("Turno Activo", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            persistShift("Turno Borrado", store, true,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Turno Activo");
        }

        @Test
        void shouldNotReturnShiftsFromAnotherStore() {
            // given
            Store otherStore = Store.builder().name("Otra Sucursal").build();
            entityManager.persistAndFlush(otherStore);

            persistShift("Turno Store A", store, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            persistShift("Turno Store B", otherStore, false,
                    LocalDate.of(2025, 1, 1), null, UUID.randomUUID());
            entityManager.clear();

            // when
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Turno Store A");
        }

        @Test
        void shouldReturnEmptyList_whenNoShiftsMatch() {
            // given - no shifts persisted
            entityManager.clear();

            // when
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldIncludeShift_whenEffectiveToEqualsStartDate() {
            // given
            persistShift("Turno Boundary", store, false,
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 1), UUID.randomUUID());
            entityManager.clear();

            // when - startDate equals shift's effectiveTo
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 12, 31));

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        void shouldIncludeShift_whenEffectiveFromEqualsEndDate() {
            // given
            persistShift("Turno Boundary", store, false,
                    LocalDate.of(2025, 6, 30), LocalDate.of(2025, 12, 31), UUID.randomUUID());
            entityManager.clear();

            // when - endDate equals shift's effectiveFrom
            List<Shift> result = shiftRepository.findActiveShiftsByStoreIdAndDateRange(
                    store.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));

            // then
            assertThat(result).hasSize(1);
        }
    }
}
