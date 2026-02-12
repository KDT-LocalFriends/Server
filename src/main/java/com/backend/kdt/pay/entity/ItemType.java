package com.backend.kdt.pay.entity;

public enum ItemType {
    CONSUMPTION("소모품"),
    COSMETIC("치장품");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}