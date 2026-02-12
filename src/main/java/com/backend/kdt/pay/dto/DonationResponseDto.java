package com.backend.kdt.pay.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonationResponseDto {
    private Long userId;
    private String donationTarget;
    private int donationAmount;
    private Long remainingPoints;
    private int rewardCosmetic;
    private int totalCosmeticItems;
    private String message;
    private LocalDateTime donatedAt;
}