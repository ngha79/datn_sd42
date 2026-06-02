package com.base.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pos_shifts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PosShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Long shiftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "opening_cash", precision = 15, scale = 2)
    private BigDecimal openingCash;

    @Column(name = "closing_cash", precision = 15, scale = 2)
    private BigDecimal closingCash;

    @Column(columnDefinition = "TEXT")
    private String note;
}
