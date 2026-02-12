package com.backend.kdt.shop.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemPurchaseResponseDto {
    private Long userId;
    private String itemName;
    private String emoji;
    private int itemPrice;
    private int quantity;
    private int totalCost;
    private int remainingPoints;
    private String message;
    private LocalDateTime purchasedAt;
}