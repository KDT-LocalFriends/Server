package com.backend.kdt.inventory.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumptionInventoryDto {
    private String categoryName; // "소모품"
    private int totalCount; // 전체 소모품 개수
    private String description;
    private List<ConsumptionItemDto> items; // 각 소모품 아이템들
}