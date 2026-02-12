package com.backend.kdt.pay.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchCompletionResponseDto {
    private Long userId;
    private int rewardItems; // 지급받은 소모품 개수
    private int totalConsumptionItems; // 총 보유 소모품 개수
    private String message;
    private LocalDateTime completedAt;
}