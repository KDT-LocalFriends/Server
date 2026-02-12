package com.backend.kdt.shop.dto;

import com.backend.kdt.pay.entity.ItemType;
import com.backend.kdt.shop.entity.ShopItemType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemTypeDto {
    private ShopItemType type;
    private String displayName;
    private String emoji;
    private int price;
    private ItemType category;
    private String categoryName;
}