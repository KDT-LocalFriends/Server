package com.backend.kdt.shop.entity;

import com.backend.kdt.pay.entity.ItemType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ShopItemType {
    // 꾸미기 아이템 (CONSUMPTION)
    PERSIMMON("단감", ItemType.CONSUMPTION, "🍊", 100),
    GREEN_TEA("정관차", ItemType.CONSUMPTION, "🍵", 100),

    // 성장 아이템 (COSMETIC)
    STRAWBERRY_HAIRPIN("딸기 헤어핀", ItemType.COSMETIC, "🍓", 100),
    GONGBANG_AHJIMA("공방 압치마", ItemType.COSMETIC, "👘", 100),
    CAR_CROWN("카아 금관", ItemType.COSMETIC, "👑", 100),
    ROSE("장미", ItemType.COSMETIC, "🌹", 100);

    private final String displayName;
    private final ItemType category;
    private final String emoji;
    private final int price;

    ShopItemType(String displayName, ItemType category, String emoji, int price) {
        this.displayName = displayName;
        this.category = category;
        this.emoji = emoji;
        this.price = price;
    }

    public String getDisplayName() { return displayName; }
    public ItemType getCategory() { return category; }
    public String getEmoji() { return emoji; }
    public int getPrice() { return price; }
}