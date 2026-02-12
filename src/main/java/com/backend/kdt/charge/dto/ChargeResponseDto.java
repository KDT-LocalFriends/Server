package com.backend.kdt.charge.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChargeResponseDto {
    private Long userId;
    private String chargeName;
    private int chargedPoints;
    private int newTotalPoints; // 충전 후 총 포인트
    private LocalDateTime chargedAt;
}