package com.abanoj.scheman.shift;

import com.abanoj.scheman.shift.entity.Shift;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftTest {

    @Nested
    @DisplayName("isCrossesMidnight")
    class IsCrossesMidnight {

        @Test
        void shouldReturnFalse_whenShiftIsWithinSameDay(){
            //given
            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .build();
            //when
            boolean result = shift.isCrossesMidnight();
            //then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnTrue_whenShiftCrossesMidnight(){
            //given
            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(22, 0))
                    .endTime(LocalTime.of(6, 0))
                    .build();
            //when
            boolean result = shift.isCrossesMidnight();
            //then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getTotalHours")
    class GetTotalHours {

        @Test
        void shouldReturnCorrectHours_whenShiftIsWithinSameDay(){
            //given
            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .build();
            //when
            double result = shift.getTotalHours();
            //then
            assertThat(result).isEqualTo(8.0);
        }

        @Test
        void shouldReturnCorrectHours_whenShiftCrossesMidnight(){
            //given
            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(22, 0))
                    .endTime(LocalTime.of(6, 0))
                    .build();
            //when
            double result = shift.getTotalHours();
            //then
            assertThat(result).isEqualTo(8.0);
        }

        @Test
        void shouldReturnCorrectHours_whenShiftHasFractionalHours(){
            //given
            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(13, 30))
                    .build();
            //when
            double result = shift.getTotalHours();
            //then
            assertThat(result).isEqualTo(4.5);
        }
    }
}
