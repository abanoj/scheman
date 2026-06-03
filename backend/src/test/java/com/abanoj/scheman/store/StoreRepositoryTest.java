package com.abanoj.scheman.store;

import com.abanoj.scheman.config.JpaAuditingConfig;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Store persistStore(String name, boolean deleted) {
        Store store = Store.builder()
                .name(name)
                .deleted(deleted)
                .build();
        entityManager.persistAndFlush(store);
        return store;
    }

    @Nested
    @DisplayName("findWithShiftsByIdAndDeletedFalse")
    class FindWithShiftsByIdAndDeletedFalse {

        @Test
        void shouldReturnStoreWithShifts_whenExistsAndNotDeleted() {
            //given
            Store store = persistStore("San Blas", false);
            Shift shift = Shift.builder()
                    .name("Turno Tarde")
                    .startTime(LocalTime.of(15,30))
                    .endTime(LocalTime.of(23,30))
                    .shiftType(ShiftType.AFTERNOON)
                    .store(store)
                    .build();
            entityManager.persistAndFlush(shift);
            entityManager.clear();
            //when
            Optional<Store> result = storeRepository.findWithShiftsByIdAndDeletedFalse(store.getId());
            //then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("San Blas");
            assertThat(result.get().getShifts())
                    .hasSize(1)
                    .extracting(Shift::getId)
                    .containsExactly(shift.getId());
        }

        @Test
        void shouldReturnStoreWithEmptyShifts_whenHasNoShifts() {
            //given
            Store store = persistStore("Lumiares", false);
            //when
            Optional<Store> result = storeRepository.findWithShiftsByIdAndDeletedFalse(store.getId());
            //then
            assertThat(result).isPresent();
            assertThat(result.get().getShifts()).isEmpty();
        }

        @Test
        void shouldReturnStoreWithEmptyShifts_whenShiftsAreSoftDeleted() {
            //given
            Store store = persistStore("Lumiares", false);
            Shift shift = Shift.builder()
                    .name("Turno Tarde")
                    .startTime(LocalTime.of(15,30))
                    .endTime(LocalTime.of(23,30))
                    .shiftType(ShiftType.AFTERNOON)
                    .store(store)
                    .deleted(true)
                    .build();
            entityManager.persistAndFlush(shift);
            entityManager.clear();
            //when
            Optional<Store> result = storeRepository.findWithShiftsByIdAndDeletedFalse(store.getId());
            //then
            assertThat(result).isPresent();
            assertThat(result.get().getShifts()).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenStoreIsDeleted() {
            //given
            Store store = persistStore("Plaza de Toros", true);
            //when
            Optional<Store> result = storeRepository.findWithShiftsByIdAndDeletedFalse(store.getId());
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenIdDoesNotExist() {
            //given
            persistStore("Plaza de Toros", false);
            //when
            Optional<Store> result = storeRepository.findWithShiftsByIdAndDeletedFalse(UUID.randomUUID());
            //then
            assertThat(result).isEmpty();
        }
    }
}
