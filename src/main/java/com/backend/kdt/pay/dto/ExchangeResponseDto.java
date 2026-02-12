package com.backend.kdt.pay.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeResponseDto {
    private Long userId;
    private String productName;
    private int quantity;
    private int totalCost;
    private Long remainingPoints;
    private int rewardCosmetic;
    private int totalCosmeticItems;
    private String message;
    private LocalDateTime exchangedAt;
}