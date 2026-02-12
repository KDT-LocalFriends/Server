package com.backend.kdt.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumptionItemDto {
    private String itemName;
    private String emoji;
    private int count; // 개수
    private String itemType; // "PERSIMMON", "GREEN_TEA"
}