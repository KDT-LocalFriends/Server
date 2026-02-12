package com.backend.kdt.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventorySummaryDto {
    private Long userId;
    // 소모품 관련
    private int totalConsumptionCount;
    private int persimmonCount;
    private int greenTeaCount;

    // 치장품 관련 (착용/소유 상태)
    private int totalOwnedCosmeticCount; // 소유한 치장품 종류 수
    private int totalEquippedCosmeticCount; // 착용 중인 치장품 수
    private boolean hasStrawberryHairpin;
    private boolean hasGongbangAhjima;
    private boolean hasCarCrown;
    private boolean hasRose;

    private int totalItemCount; // 소모품 개수 + 소유한 치장품 종류 수
}