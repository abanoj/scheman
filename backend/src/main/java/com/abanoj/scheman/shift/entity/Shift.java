package com.abanoj.scheman.shift.entity;

import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift")
public class Shift extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    private LocalTime startTime;
    private LocalTime endTime;

    @Transient
    public boolean isCrossesMidnight() {
        return startTime != null && endTime != null && startTime.isAfter(endTime);
    }
}
