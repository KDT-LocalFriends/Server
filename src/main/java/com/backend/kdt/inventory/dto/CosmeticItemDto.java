package com.backend.kdt.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CosmeticItemDto {
    private String itemName;
    private String emoji;
    private boolean isOwned; // 소유여부 (0개면 false, 1개 이상이면 true)
    private boolean isEquipped; // 착용여부
    private String itemType; // "STRAWBERRY_HAIRPIN" 등
}