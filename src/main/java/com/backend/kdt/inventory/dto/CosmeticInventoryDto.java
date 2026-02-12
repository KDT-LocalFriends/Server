package com.backend.kdt.inventory.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CosmeticInventoryDto {
    private String categoryName; // "치장품"
    private int totalOwnedCount; // 소유한 치장품 종류 수
    private int totalEquippedCount; // 착용 중인 치장품 수
    private String description;
    private List<CosmeticItemDto> items; // 각 치장품 아이템들
}