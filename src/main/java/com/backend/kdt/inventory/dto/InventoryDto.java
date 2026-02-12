package com.backend.kdt.inventory.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryDto {
    private Long userId;
    private String userName;
    private ConsumptionInventoryDto consumption;
    private CosmeticInventoryDto cosmetic;
    private LocalDateTime lastUpdated;
}